package net.redstonecraft.vulkan.vk

import net.redstonecraft.vulkan.vk.interfaces.IHandle
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK12.*
import org.lwjgl.vulkan.VkMemoryAllocateInfo
import org.lwjgl.vulkan.VkMemoryRequirements
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties

class VulkanDeviceMemory internal constructor(private val buffer: VulkanBuffer): IHandle<Long> {

    override val handle: Long

    init {
        MemoryStack.stackPush().use { stack ->
            val memoryRequirements = VkMemoryRequirements.calloc(stack)
            vkGetBufferMemoryRequirements(buffer.device.handle, buffer.handle, memoryRequirements)
            val physicalDeviceMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc(stack)
            vkGetPhysicalDeviceMemoryProperties(buffer.device.physicalDevice.handle, physicalDeviceMemoryProperties)
            val allocInfo = VkMemoryAllocateInfo.calloc(stack).`sType$Default`()
                .allocationSize(memoryRequirements.size())
                .memoryTypeIndex(findIndex(memoryRequirements.memoryTypeBits(), physicalDeviceMemoryProperties))
            val pDeviceMemory = stack.callocLong(1)
            val ret = vkAllocateMemory(buffer.device.handle, allocInfo, null, pDeviceMemory)
            if (ret != VK_SUCCESS) {
                throw VulkanException("vkAllocateMemory failed", ret)
            }
            handle = pDeviceMemory.get(0)
        }
    }

    private fun findIndex(typeFilter: Int, physicalDeviceMemoryProperties: VkPhysicalDeviceMemoryProperties): Int {
        val properties = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        val memoryTypes = physicalDeviceMemoryProperties.memoryTypes()
        for (i in 0 until physicalDeviceMemoryProperties.memoryTypeCount()) {
            if (typeFilter and (1 shl i) != 0 && (memoryTypes.get(i).propertyFlags() and properties) == properties) {
                return i
            }
        }
        throw VulkanException("no suitable memory found for buffer")
    }

    override fun close() {
        vkFreeMemory(buffer.device.handle, handle, null)
    }

}
