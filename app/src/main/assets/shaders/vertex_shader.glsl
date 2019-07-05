#version 300 es

uniform mat4 uMVPMatrix;
in vec4 vPosition;

void main() {
    gl_Position = uMVPMatrix * vPosition;
}
