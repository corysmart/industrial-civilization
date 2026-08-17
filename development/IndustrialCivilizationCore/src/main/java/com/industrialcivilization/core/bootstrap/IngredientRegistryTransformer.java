package com.industrialcivilization.core.bootstrap;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Replaces Ingredient's unsafe WeakHashMap-backed global set with a concurrent
 * map. Forge 1.12 can construct ingredients on worker threads; concurrent puts
 * can otherwise corrupt WeakHashMap's bucket chain and hang the client forever.
 */
public final class IngredientRegistryTransformer implements IClassTransformer {
    private static final String TARGET = "net.minecraft.item.crafting.Ingredient";
    private static final String UNSAFE_MAP = "java/util/WeakHashMap";
    private static final String SAFE_MAP = "java/util/concurrent/ConcurrentHashMap";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        int replacements = 0;
        for (MethodNode method : node.methods) {
            if (!"<clinit>".equals(method.name)) continue;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction instanceof TypeInsnNode) {
                    TypeInsnNode type = (TypeInsnNode) instruction;
                    if (UNSAFE_MAP.equals(type.desc)) {
                        type.desc = SAFE_MAP;
                        replacements++;
                    }
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (UNSAFE_MAP.equals(call.owner) && "<init>".equals(call.name)) {
                        call.owner = SAFE_MAP;
                        replacements++;
                    }
                }
            }
        }
        if (replacements != 2) {
            throw new IllegalStateException("Ingredient registry guard expected 2 bytecode replacements, found "
                + replacements);
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
