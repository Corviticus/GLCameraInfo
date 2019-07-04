#version 300 es

uniform mat4 uMVPMatrix;

in vec4 vPosition;

// The matrix must be included as a modifier of gl_Position.
// Note that the uMVPMatrix factor *must be first* in order
// for the matrix multiplication product to be correct.
void main() {
    gl_Position = uMVPMatrix * vPosition;
}