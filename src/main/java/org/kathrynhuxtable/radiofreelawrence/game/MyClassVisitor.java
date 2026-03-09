package org.kathrynhuxtable.radiofreelawrence.game;

import org.objectweb.asm.ClassWriter;

public class MyClassVisitor extends ClassWriter {

	public MyClassVisitor(int flags) {
		super(flags);
	}

	public void createField(int flags, String name, String internalType) {
		createField(flags, name, internalType, null);
	}

	public void createField(int flags, String name, String internalType, String signature) {
		visitField(flags, name, internalType, signature, null).visitEnd();
	}
}
