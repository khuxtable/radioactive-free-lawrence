package org.kathrynhuxtable.radiofreelawrence.game;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

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
}
