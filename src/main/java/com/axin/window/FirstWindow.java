package com.axin.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class FirstWindow {

  private float red = 0.0f;
  private float green = 0.0f;
  private float blue = 0.0f;

  private long window;
  double lastSwitch = 0;

  public void run() {
    if (!GLFW.glfwInit()) {
      throw new IllegalStateException();
    }

    this.window = GLFW.glfwCreateWindow(800, 600, "First Window", 0, 0);

    if (window == 0) {
      throw new RuntimeException("Failed to create window");
    }

    GLFW.glfwMakeContextCurrent(window);
    GL.createCapabilities();
    GLFW.glfwShowWindow(window);


    while (!GLFW.glfwWindowShouldClose(window)) {
      process();
    }

    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
  }
  private void process() {
    init();

    handleKey();

    GL11.glClearColor(red, green, blue, 1.0f);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    GLFW.glfwSwapBuffers(window);
    GLFW.glfwPollEvents();
  }

  private void init() {
    red = 0.0f;
    green = 0.0f;
    blue = 0.0f;
  }

  private void handleKey() {
    handleWKey();
    handleSKey();
  }

  private void handleWKey() {
    if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS){
      red = 1.0f;
      green = 0.0f;
      blue = 0.0f;
    }
  }

  private void handleSKey() {
    if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS){
      red = 0.0f;
      green = 0.0f;
      blue = 1.0f;
    }
  }
}
