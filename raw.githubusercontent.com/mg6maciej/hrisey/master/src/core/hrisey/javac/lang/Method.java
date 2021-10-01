/*
 * Copyright (C) 2014 Maciej Gorski
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hrisey.javac.lang;

import static hrisey.javac.lang.EmptyList.*;
import static lombok.javac.handlers.JavacHandlerUtil.injectMethod;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.ListBuffer;

import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

public class Method {
	
	private final List<Modifier> modifiers;
	private final TypeExpression returnType;
	private final String name;
	private final List<Parameter> parameters;
	private final Block body;

	public Method(List<Modifier> modifiers, List<Parameter> parameters, Block body) {
		this(modifiers, null, "<init>", parameters, body);
	}

	public Method(List<Modifier> modifiers, TypeExpression returnType, String name, List<Parameter> parameters, Block body) {
		this.modifiers = modifiers;
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
		this.body = body;
	}
	
	public void inject(JavacNode classNode) {
		JavacTreeMaker maker = classNode.getTreeMaker();
		ListBuffer<JCVariableDecl> list = new ListBuffer<JCVariableDecl>();
		for (Parameter parameter : parameters) {
			list.add(parameter.create(classNode));
		}
		JCMethodDecl method = maker.MethodDef(
				Modifier.toJavac(maker, modifiers),
				classNode.toName(name),
				returnType != null ? returnType.create(classNode) : null,
				emptyTypeParameters(),
				list.toList(),
				emptyExpressions(),
				body.create(classNode),
				null);
		injectMethod(classNode, method);
	}
}
