package com.industrialcivilization.core;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.industrialcivilization.core.bootstrap.IngredientRegistryTransformer;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class IngredientRegistryTransformerTest {
    @Test
    public void replacesUnsafeIngredientRegistryMap() throws Exception {
        byte[] original = readClass("net/minecraft/item/crafting/Ingredient.class");
        byte[] transformed = new IngredientRegistryTransformer().transform(
            "net.minecraft.item.crafting.Ingredient",
            "net.minecraft.item.crafting.Ingredient",
            original);

        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        int weakReferences = 0;
        int concurrentReferences = 0;
        for (MethodNode method : node.methods) {
            if (!"<clinit>".equals(method.name)) continue;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                String owner = null;
                if (instruction instanceof TypeInsnNode) owner = ((TypeInsnNode) instruction).desc;
                if (instruction instanceof MethodInsnNode) owner = ((MethodInsnNode) instruction).owner;
                if ("java/util/WeakHashMap".equals(owner)) weakReferences++;
                if ("java/util/concurrent/ConcurrentHashMap".equals(owner)) concurrentReferences++;
            }
        }
        assertEquals(0, weakReferences);
        assertEquals(2, concurrentReferences);
    }

    private static byte[] readClass(String resource) throws Exception {
        try (InputStream input = IngredientRegistryTransformerTest.class.getClassLoader()
            .getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Missing test class resource " + resource);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) output.write(buffer, 0, read);
            return output.toByteArray();
        }
    }
}
