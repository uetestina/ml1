/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.transform.hash;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.process.InstructionGroupPreparer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;

/**
 * Hashing for an {@link InstructionGroup}
 *
 * <p>Not very pretty, that one.</p>
 *
 * <p>The only entry point, {@link #hash(InstructionGroup, String)}, will hash
 * the entire instructions and field out of the instruction group, then set
 * the name of the instruction group appropriately.</p>
 *
 * @see InstructionGroupPreparer
 */
//TODO: use more than 16 chars; means updating all string-based bytecode tests
@NotThreadSafe
public final class InstructionGroupHasher
    extends MethodVisitor
{
    private static final BaseEncoding BASE_ENCODING
        = BaseEncoding.base32Hex();
    private static final HashFunction SHA1 = Hashing.sha1();

    private final String className;
    private final InstructionGroup group;

    private final Hasher hasher;
    private final LabelListFunnel labelFunnel = new LabelListFunnel();

    /**
     * Generate a hash of the group, use it to name it
     *
     * @param group the instruction group
     * @param className this group's parent class name
     */
    public static void hash(@Nonnull final InstructionGroup group,
        @Nonnull final String className)
    {
        final InstructionGroupHasher groupHasher
            = new InstructionGroupHasher(group, className);
        final String name = groupHasher.hashAndGetName();
        group.setName(name);
    }

    private InstructionGroupHasher(@Nonnull final InstructionGroup group,
        @Nonnull final String className)
    {
        super(Opcodes.ASM5);
        this.group = Objects.requireNonNull(group);
        this.className = Objects.requireNonNull(className);
        hasher = SHA1.newHasher();
    }

    // TODO: not very nice :/
    private String hashAndGetName()
    {
        group.getInstructions().accept(this);
        for (final FieldNode node: group.getFields())
            hasher.putObject(node, FieldNodeFunnel.INSTANCE);
        final byte[] hash = new byte[10];
        hasher.hash().writeBytesTo(hash, 0, 10);
        final String prefix = group.getRoot().isActionRoot()
            ? "Action$" : "VarInit$";
        return prefix + BASE_ENCODING.encode(hash);
    }

    @Override
    public void visitInsn(final int opcode)
    {
        hasher.putInt(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand)
    {
        hasher.putInt(opcode).putInt(operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var)
    {
        hasher.putInt(opcode).putInt(var);
        /*
         * (copied from original code) Make sure the names of identical actions
         * differ if thet are defined in different parent classes
         *
         * TODO: why var == 0? What does it mean?
         */
        if (opcode == Opcodes.ALOAD && var == 0)
            hasher.putUnencodedChars(className);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type)
    {
        hasher.putInt(opcode).putUnencodedChars(type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
        final String name, final String desc)
    {
        hasher.putInt(opcode).putUnencodedChars(owner)
            .putUnencodedChars(name).putUnencodedChars(desc);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
        final String name, final String desc, final boolean itf)
    {
        hasher.putInt(opcode).putUnencodedChars(owner)
            .putUnencodedChars(name).putUnencodedChars(desc);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label)
    {
        hasher.putInt(opcode).putObject(label, labelFunnel);
    }

    @Override
    public void visitLabel(final Label label)
    {
        hasher.putObject(label, labelFunnel);
    }

    @Override
    public void visitLdcInsn(final Object cst)
    {
        hasher.putObject(cst, LdcInsnFunnel.INSTANCE);
    }

    @Override
    public void visitIincInsn(final int var, final int increment)
    {
        hasher.putInt(var).putInt(increment);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
        final Label dflt, final Label... labels)
    {
        hasher.putInt(min).putInt(max).putObject(dflt, labelFunnel);
        for (final Label label: labels)
            hasher.putObject(label, labelFunnel);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
        final Label[] labels)
    {
        hasher.putObject(dflt, labelFunnel);
        for (int i = 0; i < keys.length; i++)
            hasher.putInt(keys[i]).putObject(labels[i], labelFunnel);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims)
    {
        hasher.putUnencodedChars(desc).putInt(dims);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end,
        final Label handler, final String type)
    {
        hasher.putObject(start, labelFunnel).putObject(end, labelFunnel)
            .putObject(handler, labelFunnel).putUnencodedChars(type);
    }
}
