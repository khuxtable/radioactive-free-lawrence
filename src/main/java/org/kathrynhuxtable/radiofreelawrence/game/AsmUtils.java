package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class AsmUtils {

	// ClassVisitor helpers

	public static void createField(ClassVisitor cv, int flags, String name, String internalType) {
		createField(cv, flags, name, internalType, null);
	}

	public static void createField(ClassVisitor cv, int flags, String name, String internalType, String signature) {
		cv.visitField(flags, name, internalType, signature, null).visitEnd();
	}

	// MethodVisitor helpers

	public static void assignVariable(MethodVisitor mv, String internalClassName, String name, String descriptor, Object value) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(value);
		mv.visitFieldInsn(PUTFIELD, internalClassName, name, descriptor);
	}

	public static void createGetter(ClassVisitor cv, String innerClassInternalName, int flags, String getterName, String name, String descriptor) {
		createGetter(cv, innerClassInternalName, flags, getterName, name, descriptor, null);
	}

	public static void createGetter(ClassVisitor cv, String innerClassInternalName, int flags, String getterName, String name, String descriptor, String signature) {
		String innerClassDescriptor = "L" + innerClassInternalName + ";";
		MethodVisitor mv = cv.visitMethod(flags, getterName, "()" + descriptor, "()" + signature, null);
		mv.visitCode();
		Label beginLabel = new Label();
		mv.visitLabel(beginLabel);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, innerClassInternalName, name, descriptor);
		mv.visitInsn(ARETURN);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		mv.visitLocalVariable("this", innerClassDescriptor, null, beginLabel, endLabel, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	public static void createSetter(ClassVisitor cv, String innerClassInternalName, String setterName, String name, String descriptor) {
		createSetter(cv, innerClassInternalName, setterName, name, descriptor, null);
	}

	public static void createSetter(ClassVisitor cv, String innerClassInternalName, String setterName, String name, String descriptor, String signature) {
		String innerClassDescriptor = "L" + innerClassInternalName + ";";
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, setterName, "(" + descriptor + ")V", signature, null);
		mv.visitParameter(name, 0);
		mv.visitCode();
		Label beginLabel = new Label();
		mv.visitLabel(beginLabel);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(PUTFIELD, innerClassInternalName, name, Type.getDescriptor(GamePlace.class));
		mv.visitInsn(RETURN);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		mv.visitLocalVariable("this", innerClassDescriptor, null, beginLabel, endLabel, 0);
		mv.visitLocalVariable("location", descriptor, null, beginLabel, endLabel, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	public static <T> void createList(MethodVisitor mv, String innerClassInternalName, String name, Iterable<T> iterable) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, Type.getInternalName(ArrayList.class));
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(ArrayList.class), "<init>", "()V", false);
		mv.visitFieldInsn(PUTFIELD, innerClassInternalName, name, Type.getDescriptor(List.class));
		if (iterable != null) {
			for (T action : iterable) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, innerClassInternalName, name, Type.getDescriptor(List.class));
				mv.visitLdcInsn(action);
				mv.visitMethodInsn(
						INVOKEINTERFACE,
						Type.getInternalName(List.class),
						"add",
						"(Ljava/lang/Object;)Z",
						true
				);
			}
		}
	}
}
