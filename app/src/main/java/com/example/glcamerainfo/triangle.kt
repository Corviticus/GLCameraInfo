package com.example.glcamerainfo

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.GLES30

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
class Triangle(private val mRenderer: GLRenderer) {

    private val vertexBuffer: FloatBuffer
    private val mProgram: Int
    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle: Int = 0

    // this will be 3 for a triangle
    private val vertexCount = triangleCoordinates.size / COORDINATES_PER_VERTEX

    // 4 bytes per vertex
    private val vertexStride = COORDINATES_PER_VERTEX * 4

    // this is the accent triangleColor from the app <triangleColor extensionName="colorAccent">#D35400</triangleColor>
    private val red = "D3".toLong(16) / 255f
    private val green = "54".toLong(16) / 255f
    private val blue = "00".toLong(16) / 255f
    private var triangleColor = floatArrayOf(red, green, blue, 0.0f)

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    init {

        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
            // (number of coordinate values * 4 bytes per float)
            triangleCoordinates.size * 4
        )

        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoordinates)

        // set the buffer to read the first coordinate
        vertexBuffer.position(0)

        // compile the vertex and fragment shader glsl files
        mProgram = mRenderer.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    fun draw(mvpMatrix: FloatArray) {

        // Add program to OpenGL environment
        GLES30.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
            mPositionHandle, COORDINATES_PER_VERTEX,
            GLES30.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor")

        // Set triangleColor for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, triangleColor, 0)

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        mRenderer.checkGlError("glGetUniformLocation")

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        mRenderer.checkGlError("glUniformMatrix4fv")

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {

        private const val VERTEX_SHADER = "shaders/vertex_shader.glsl"
        private const val FRAGMENT_SHADER = "shaders/fragment_shader.glsl"

        // number of coordinates per vertex in this array
        internal const val COORDINATES_PER_VERTEX = 3

        // in counterclockwise order
        internal var triangleCoordinates = floatArrayOf(
            0.0f, 0.622008459f, 0.0f,   // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f   // bottom right
        )
    }
}