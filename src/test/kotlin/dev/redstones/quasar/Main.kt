package dev.redstones.quasar

import dev.redstones.quasar.shaderc.GLSLCompiler
import dev.redstones.quasar.vfs.ResourceVFS
import org.lwjgl.glfw.GLFW.*
import kotlin.system.exitProcess

const val width = 1280
const val height = 720

fun main() {

    val compiler = GLSLCompiler(ResourceVFS("/shaders"))

    if (glfwPlatformSupported(GLFW_PLATFORM_WAYLAND)) {
//        glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_WAYLAND)
    }
    if (!glfwInit()) {
        exitProcess(-1)
    }
    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
    val window = glfwCreateWindow(width, height, "Vulkan Test", 0, 0)

    val vulkan = VulkanContext(window, compiler, "test", "Vulkan Test", Triple(0, 1, 0))

    var time = System.nanoTime()
    var frames = 0

    val render = { window: Long ->
        vulkan.drawFrame()
        frames++
        if ((System.nanoTime() - time) >= 1000000000) {
            println(frames)
            frames = 0
            time = System.nanoTime()
        }
    }

    glfwSetWindowRefreshCallback(window, render)

    glfwSetFramebufferSizeCallback(window) { window: Long, width: Int, height: Int ->
        vulkan.notifyResize()
    }

    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents()
        render(window)
    }

    vulkan.close()
    glfwDestroyWindow(window)
    glfwTerminate()
}
