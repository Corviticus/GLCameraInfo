package com.example.cameracheckup

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.GLES20

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
class Triangle {

    private val vertexShaderCode = // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}"

    private var mRenderer = GLRenderer()

    private val vertexBuffer: FloatBuffer
    private val mProgram: Int
    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle: Int = 0

    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX

    // 4 bytes per vertex
    private val vertexStride = COORDS_PER_VERTEX * 4

    // triangle color as RGBA

    // this is the accent color from the app <color name="colorAccent">#D35400</color>
    private var color = floatArrayOf(211.toFloat()/255, 84.toFloat() / 255, 0f, 0.0f)


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    init {

        // initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(
            // (number of coordinate values * 4 bytes per float)
            triangleCoords.size * 4
        )

        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer()

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords)

        // set the buffer to read the first coordinate
        vertexBuffer.position(0)

        // prepare shaders and OpenGL program
        val vertexShader = mRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = mRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL Program
        mProgram = GLES20.glCreateProgram()

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader)

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader)

        // create OpenGL program executables
        GLES20.glLinkProgram(mProgram)
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    fun draw(mvpMatrix: FloatArray) {
        
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        mRenderer.checkGlError("glGetUniformLocation")

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        mRenderer.checkGlError("glUniformMatrix4fv")

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {

        // number of coordinates per vertex in this array
        internal const val COORDS_PER_VERTEX = 3

        // in counterclockwise order
        internal var triangleCoords = floatArrayOf(
            0.0f, 0.622008459f, 0.0f,   // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f   // bottom right
        )
    }
}