package com.example.cameracheckup


import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import android.opengl.GLES30

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
class Square(private val mRenderer: GLRenderer) {

    private val vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private var mProgram: Int

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle: Int = 0

    // order to draw vertices
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    // 4 bytes per vertex
    private val vertexStride = COORDS_PER_VERTEX * 4

    // this is the primary squareColor from the app <squareColor extensionName="colorPrimary">#303F9F</squareColor>
    private val red = "30".toLong(16) / 255f
    private val green = "3F".toLong(16) / 255f
    private val blue = "9F".toLong(16) / 255f
    private var squareColor = floatArrayOf(red, green, blue, 0.0f)
    /**
     * Set up the drawing object data for use in an OpenGL ES context
     */
    init {

        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 4 bytes per float)
            squareCoordinates.size * 4
        )

        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(squareCoordinates)
        vertexBuffer.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 2 bytes per short)
            drawOrder.size * 2
        )

        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

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
            mPositionHandle, COORDS_PER_VERTEX,
            GLES30.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor")

        // Set squareColor for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, squareColor, 0)

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        mRenderer.checkGlError("glGetUniformLocation")

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        mRenderer.checkGlError("glUniformMatrix4fv")

        // Draw the square
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES, drawOrder.size,
            GLES30.GL_UNSIGNED_SHORT, drawListBuffer
        )

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {

        private const val VERTEX_SHADER = "shaders/vertex_shader.glsl"
        private const val FRAGMENT_SHADER = "shaders/fragment_shader.glsl"

        // number of coordinates per vertex in this array
        internal const val COORDS_PER_VERTEX = 3

        internal var squareCoordinates = floatArrayOf(
            -0.5f, 0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            0.5f, 0.5f, 0.0f    // top right
        )
    }

}
