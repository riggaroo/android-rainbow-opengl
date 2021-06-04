package dev.riggaroo.rainbowbox

import org.intellij.lang.annotations.Language

// Shader that runs for each vertex geometry that is passed in
@Language("glsl")
val VERTEX_SHADER = """
    #version 300 es
    precision highp float;
    precision highp int;
    
    uniform highp mat4 uModelMatrix;
    uniform highp mat4 uViewProjMatrix;
    uniform highp float uStrokeWidth;
    uniform highp float uAspectRatio;
    
    layout (location = 0) in vec2 aPosition;
    
    // Offset represents the direction in which this point should be shifted to form the border
    layout (location = 1) in vec2 aOffset;
    
    // Progress changes from 0.0 to 1.0 along the perimeter (does not account for scaling, not yet).
    layout (location = 2) in float aProgress;
    
    out float vProgress;
    
    // This version of normalize() 'correctly' handles zero-length vectors
    vec2 safeNormalize(vec2 v) {
        if (length(v) == 0.0) return v;
        return normalize(v);
    }
    
    void main() {
        float aspectRatio = uAspectRatio;
        vProgress = aProgress;
        vec4 worldPosition = uModelMatrix * vec4(aPosition, 0.0, 1.0);
    
        // We need to get the correct direction for the offset that forms the border (the thickness of the bounding box).
        // For that we see where the point ends up in the 'world' coordinates, then correct by aspect ratio to account for scaling,
        // and then normalize. Ta-da, offset direction!
        vec4 offsetPosition = uModelMatrix * vec4(aPosition + aOffset, 0.0, 1.0);
        vec2 difference = offsetPosition.xy - worldPosition.xy;
        vec4 offset = vec4(safeNormalize(difference) * uStrokeWidth, 0.0, 0.0);
        gl_Position = uViewProjMatrix * (worldPosition + offset);
    }
    """.trimIndent()

// Shader that runs for each pixel of the computed area - derived from the vertex shader output
@Language("glsl")
val FRAGMENT_SHADER = """
    #version 300 es

    precision highp float;

    uniform highp float uAspectRatio;
    uniform highp float uDashCount;
    uniform highp float uTimeOffset;

    in highp float vProgress;

    const vec4 COLORS[7] = vec4[](
        vec4(1.0000, 0.1490, 0.2196, 1.0),
        vec4(1.0000, 0.4196, 0.1882, 1.0),
        vec4(1.0000, 0.6353, 0.0078, 1.0),
        vec4(0.0078, 0.8157, 0.5686, 1.0),
        vec4(0.0039, 0.5020, 0.9843, 1.0),
        vec4(0.4824, 0.2118, 0.8549, 1.0),
        vec4(1.0000, 0.1490, 0.2196, 1.0) // Re-adding the first color to avoid mod() operation after 'colorIndex + 1'
        );

    out vec4 oColor;

    float isInRange(float x, float start, float end) {
        return step(start, x) * (1.0 - step(end, x));
    }

    void main() {
        // We need to count the progress along the perimeter, keeping in mind that scaling along x and y is not uniform,
        // meaning that our lovely square probably turned into a rect with some wild aspect ratio.
        float aspectRatio = uAspectRatio;
        float vertLen = 1.0f;
        float horizLen = aspectRatio;
        float perimeter = vertLen * 2.0 + horizLen * 2.0;
        float vertProp = vertLen / perimeter;
        float horizProp = horizLen / perimeter;

        // Need to count the progress along the sides that we might have already passed
        float pastProgress = step(0.25, vProgress) * vertProp +
            step(0.5, vProgress) * horizProp +
            step(0.75, vProgress) * vertProp;

        // Now count the progress along the current side
        float currentSegmentSize =
            isInRange(vProgress, 0.0, 0.25) * vertProp +
            isInRange(vProgress, 0.25, 0.5) * horizProp +
            isInRange(vProgress, 0.5, 0.75) * vertProp +
            isInRange(vProgress, 0.75, 1.0) * horizProp;

        // Multiplying vProgress by 4 and getting a fraction would give us the progress along the current side.
        // Why 4? Because the number of sides.
        float currentProgress = fract(vProgress * 4.0f) * currentSegmentSize;

        // vProgress is interpolated between 0 - 1 by the vertex shader. 
        // We multiply by uTimeOffset to give the animation over time.
        // We multiply uTimeOffset by 16 to make the speed of the animation a bit faster, and 0.125 to stretch out the gradient a bit more.
        // Now bringing it all together into the final progress value that should give a nice smooth gradient along the perimeter.
        float progress = (vProgress + uTimeOffset * 16.0f) * 0.125;
        float colorIndex = mod(uDashCount * progress / 4.0, 6.0); // There are actually 6 colors, not 7
        vec4 currentColor = COLORS[int(floor(colorIndex))];
        vec4 nextColor = COLORS[int(floor(colorIndex)) + 1];
        // The output colour of the pixel is a mix between the two colors, producing the gradient effect
        oColor = mix(currentColor, nextColor, fract(colorIndex));
    }
    """.trimIndent()