package com.axin.shape;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FirstCube {

  private long window;

  private float positionX = 0;
  private float positionY = 0;
  private float positionZ = 3;

  private float yaw = -90f;
  private float pitch = 0f;

  private float lastMouseX = 400;
  private float lastMouseY = 300;

  private boolean firstMouse = true;
  private boolean mouseCaptured = true;

  private Vector3f cameraFront = new Vector3f(0, 0, -1);

  private double lastDebugTime = 0;

  public void run() {
    if (!GLFW.glfwInit()) {
      throw new IllegalStateException();
    }
    init();
    drawCubeWithEbo();
  }

  private void init() {
    // 指定 OpenGL 3.3
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

    // 使用 Core Profile, 代表只能用 Shader
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

    // macOS 必須加否則報錯
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

    // 建立視窗
    window = GLFW.glfwCreateWindow(800, 600, "First Cube", 0, 0);
    if (window == 0) {
      throw new RuntimeException("Failed to create window");
    }

    // 將 OpenGL Context 綁定到指定視窗
    GLFW.glfwMakeContextCurrent(window);

    // 載入 OpenGL 函數
    GL.createCapabilities();

    // 開啟深度測試
    GL11.glEnable(GL11.GL_DEPTH_TEST);

    // 顯示視窗
    GLFW.glfwShowWindow(window);

    mouseLog();
  }

  private void drawCubeWithEbo() {
    // 定義 Cube 八個頂點
    float[] vertices = {

            -0.5f, -0.5f, 0.5f,   // 0
            0.5f, -0.5f, 0.5f,    // 1
            0.5f, 0.5f, 0.5f,     // 2
            -0.5f, 0.5f, 0.5f,    // 3

            -0.5f, -0.5f, -0.5f,  // 4
            0.5f, -0.5f, -0.5f,   // 5
            0.5f, 0.5f, -0.5f,    // 6
            -0.5f, 0.5f, -0.5f    // 7
    };

    // 定義每個 Square 的兩個 Triangle
    int[] indices = {

            // Front
            0, 1, 2, 2, 3, 0,

            // Back
            5, 4, 7, 7, 6, 5,

            // Left
            4, 0, 3, 3, 7, 4,

            // Right
            1, 5, 6, 6, 2, 1,

            // Top
            3, 2, 6, 6, 7, 3,

            // Bottom
            4, 5, 1, 1, 0, 4};

    // 定義 vertexBuffer, indexBuffer 讓 OpenGL 接收 Java Array
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

    // 把 Vertex 上傳到 GPU
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

    int ebo = GL15.glGenBuffers();
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

    // 把 Index 上傳 GPU。
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

    // 告訴 GPU 記憶體排列
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);

    // 啟用 layout(location=0)
    GL20.glEnableVertexAttribArray(0);

    // 建立 GPU 程式
    int shaderProgram = createDefaultShader();

    int modelLocation = GL20.glGetUniformLocation(shaderProgram, "model");
    int viewLocation = GL20.glGetUniformLocation(shaderProgram, "view");
    int projectionLocation = GL20.glGetUniformLocation(shaderProgram, "projection");
    int colorLocation = GL20.glGetUniformLocation(shaderProgram, "color");

    FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // 建立透視投影
    Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70), 800f / 600f, 0.1f, 100f);

    // 主迴圈
    while (!GLFW.glfwWindowShouldClose(window)) {

      GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

      // 使用 Shader
      GL20.glUseProgram(shaderProgram);

      // 旋轉 Cube
      Matrix4f model = new Matrix4f();
//              .rotateY((float) GLFW.glfwGetTime())
//              .rotateX((float) GLFW.glfwGetTime());

      // 定義相機面向
      cameraFront.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
      cameraFront.y = (float) (Math.sin(Math.toRadians(pitch)));
      cameraFront.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

      cameraFront.normalize();

      Vector3f cameraPos = new Vector3f(positionX, positionY, positionZ);
      Vector3f target = new Vector3f(cameraPos).add(cameraFront);
      Matrix4f view = new Matrix4f().lookAt(cameraPos, target, new Vector3f(0, 1, 0));

      matrixBuffer.clear();

      // 傳送 Matrix 到 GPU
      model.get(matrixBuffer);

      GL20.glUniformMatrix4fv(modelLocation, false, matrixBuffer);

      matrixBuffer.clear();
      view.get(matrixBuffer);
      GL20.glUniformMatrix4fv(viewLocation, false, matrixBuffer);

      matrixBuffer.clear();
      projection.get(matrixBuffer);
      GL20.glUniformMatrix4fv(projectionLocation, false, matrixBuffer);

      // 綁定 VAO
      GL30.glBindVertexArray(vao);

      // 畫 Cube
      GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
      GL20.glUniform3f(colorLocation, 1.0f, 0.0f, 0.0f);
      GL11.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);

      // 畫白色編框
      GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
      GL20.glUniform3f(colorLocation, 1.0f, 1.0f, 1.0f);
      GL11.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);

      inputHandle();

      double now = GLFW.glfwGetTime();

      if (now - lastDebugTime > 1.0) {
        lastDebugTime = now;
        System.out.printf("====================\n" + "Position : (%.2f, %.2f, %.2f)\n" + "Yaw      : %.2f\n" + "Front    : (%.2f, %.2f, %.2f)\n" + "====================\n", positionX, positionY, positionZ, yaw, cameraFront.x, cameraFront.y, cameraFront.z);
      }

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

    String vertexShaderSource = "#version 330 core\n" + "layout (location = 0) in vec3 aPos;\n" + "\n" + "uniform mat4 model;\n" + "uniform mat4 view;\n" + "uniform mat4 projection;\n" + "\n" + "void main() {\n" + "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" + "}";
    String fragmentShaderSource = "#version 330 core\n" + "out vec4 FragColor;\n" + "\n" + "uniform vec3 color;\n" + "\n" + "void main() {\n" + "    FragColor = vec4(color, 1.0);\n" + "}";

    int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
    GL20.glShaderSource(vertexShader, vertexShaderSource);
    GL20.glCompileShader(vertexShader);

    int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
    GL20.glShaderSource(fragmentShader, fragmentShaderSource);
    GL20.glCompileShader(fragmentShader);

    int shaderProgram = GL20.glCreateProgram();
    GL20.glAttachShader(shaderProgram, vertexShader);
    GL20.glAttachShader(shaderProgram, fragmentShader);

    GL20.glLinkProgram(shaderProgram);

    GL20.glDeleteShader(vertexShader);
    GL20.glDeleteShader(fragmentShader);

    return shaderProgram;
  }

  private void inputHandle() {
    float speed = 0.03f;

    Vector3f right = new Vector3f(-cameraFront.z, 0, cameraFront.x).normalize();

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
      positionX += cameraFront.x * speed;
      positionZ += cameraFront.z * speed;
    }

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
      positionX -= cameraFront.x * speed;
      positionZ -= cameraFront.z * speed;
    }

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
      positionX -= right.x * speed;
      positionZ -= right.z * speed;
    }

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
      positionX += right.x * speed;
      positionZ += right.z * speed;
    }

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
      positionY += speed;
    }

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
      positionY -= speed;
    }

    if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
      mouseCaptured = false;
      GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
      if (!mouseCaptured) {
        mouseCaptured = true;
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        firstMouse = true;
      }
    }

  }

  private void mouseLog() {
    GLFW.glfwSetCursorPosCallback(window, (windowHandle, mouseX, mouseY) -> {
              if (!mouseCaptured) {
                return;
              }

              if (firstMouse) {
                lastMouseX = (float) mouseX;
                lastMouseY = (float) mouseY;
                firstMouse = false;
              }

              float offsetX = (float) mouseX - lastMouseX;

              float offsetY = lastMouseY - (float) mouseY;

              lastMouseX = (float) mouseX;
              lastMouseY = (float) mouseY;

              float sensitivity = 0.1f;

              offsetX *= sensitivity;
              offsetY *= sensitivity;

              yaw += offsetX;
              pitch += offsetY;

              if (pitch > 89f) {
                pitch = 89f;
              }

              if (pitch < -89f) {
                pitch = -89f;
              }
            }

    );

    GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
  }
}