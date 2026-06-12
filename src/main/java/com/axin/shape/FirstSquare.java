package com.axin.shape;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FirstSquare {

  private long window;

  public void run() {
    if (!GLFW.glfwInit()) {
      throw new IllegalStateException();
    }
    init();
    drawSquare();
//    drawSquareWithEbo();
  }

  private void init() {
    // 指定 OpenGL 版本 3.3
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

    // 只允許現代 OpenGL
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

    // macOS 必須加否則報錯
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

    // 建立 Window
    window = GLFW.glfwCreateWindow(800, 600, "Triangle", 0, 0);
    if (window == 0) {
      throw new RuntimeException("Failed to create window");
    }

    // 將 OpenGL 綁定到指定視窗
    GLFW.glfwMakeContextCurrent(window);

    // 載入 OpenGL 函數
    GL.createCapabilities();

    // 顯示視窗
    GLFW.glfwShowWindow(window);
  }

  private void drawSquare() {
    float[] vertices = {
            // Triangle 1
            -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f,

            // Triangle 2
            -0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};

    // 建立 Buffer, Java Array <-> OpenGL Buffer
    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
    vertexBuffer.put(vertices)
            .flip();

    // 建立 VAO
    int vao = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(vao);

    // 建立 VBO
    int vbo = GL15.glGenBuffers();
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

    // 定義 GPU 如何解讀 Vertex
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);

    // 啟用 AttribArray 0
    GL20.glEnableVertexAttribArray(0);

    int shaderProgram = createDefaultShader();

    // 主迴圈
    while (!GLFW.glfwWindowShouldClose(window)) {

      // 清空畫面
      GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

      // 使用 Shader
      GL20.glUseProgram(shaderProgram);

      // 使用 VAO
      GL30.glBindVertexArray(vao);

      // 畫三角形
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

      // 顯示畫面
      GLFW.glfwSwapBuffers(window);

      // 讀取使用者操作
      GLFW.glfwPollEvents();
    }

    GL30.glDeleteVertexArrays(vao);
    GL15.glDeleteBuffers(vbo);
    GL20.glDeleteProgram(shaderProgram);
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
  }

  private void drawSquareWithEbo() {
    float[] vertices = {
            -0.5f, -0.5f, 0.0f, // 1
            0.5f, -0.5f, 0.0f,  // 2
            0.5f,  0.5f, 0.0f,  // 3
            -0.5f,  0.5f, 0.0f  // 4
    };

    int[] indices = {
            0, 1, 2,
            0, 2, 3
    };

    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
    vertexBuffer.put(vertices)
            .flip();

    IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
    indexBuffer.put(indices)
            .flip();

    int vao = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(vao);

    int vbo = GL15.glGenBuffers();
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

    int ebo = GL15.glGenBuffers();
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
    GL20.glEnableVertexAttribArray(0);

    int shaderProgram = createDefaultShader();

    while (!GLFW.glfwWindowShouldClose(window)) {

      GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
      GL20.glUseProgram(shaderProgram);
      GL30.glBindVertexArray(vao);
      GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
      GLFW.glfwSwapBuffers(window);
      GLFW.glfwPollEvents();
    }

    GL30.glDeleteVertexArrays(vao);
    GL15.glDeleteBuffers(vbo);
    GL15.glDeleteBuffers(ebo);
    GL20.glDeleteProgram(shaderProgram);
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
  }

  private int createDefaultShader() {
    String vertexShaderSource = "#version 330 core\n" + "layout (location = 0) in vec3 aPos;\n" + "void main() {\n" + "    gl_Position = vec4(aPos, 1.0);\n" + "}";
    String fragmentShaderSource = "#version 330 core\n" + "out vec4 FragColor;\n" + "void main() {\n" + "    FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" + "}";

    // 建立 vertexShader 並上入程式碼
    int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
    GL20.glShaderSource(vertexShader, vertexShaderSource);
    GL20.glCompileShader(vertexShader);

    // 建立 fragmentShader 並上入程式碼
    int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
    GL20.glShaderSource(fragmentShader, fragmentShaderSource);
    GL20.glCompileShader(fragmentShader);

    // 建立 Vertex Shader
    int shaderProgram = GL20.glCreateProgram();

    GL20.glAttachShader(shaderProgram, vertexShader);
    GL20.glAttachShader(shaderProgram, fragmentShader);
    GL20.glLinkProgram(shaderProgram);
    GL20.glDeleteShader(vertexShader);
    GL20.glDeleteShader(fragmentShader);

    return shaderProgram;
  }
}