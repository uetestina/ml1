/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast;

import java.util.List;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Tests the LineColumn information of the groovy source obtained in the
 * source parameter of the constructor
 * 
 * @author <a href="mailto:martin.kempf@gmail.com">Martin Kempf</a>
 *
 */
public class LineColumnChecker extends ASTTest {
    
    private LineCheckVisitor visitor;
    private String name;
    private String source;
    private String[] expected;
    
    public LineColumnChecker(String name, String source, String expected) {
        this.name = name;
        this.source = source;
        this.expected = expected.split(";");
        // Set Method to call for JUnit
        setName("testLineColumn");
    }
    
    public void setUp() {
        visitor = new LineCheckVisitor();
    }
    
    public String getName() {
        return name;
    }
    
    public void testLineColumn() {
        visitor.visitModuleNode(getAST(source));
        String was = visitor.getASTString();
        //comment out next line to view the output of the visitor
        //System.out.println(name + ": " + was);
        for (String anExpected : expected) {
            assertTrue("'" + anExpected + "' not found in '" + was + "'", was.indexOf(anExpected.trim()) != -1);
        }
    }
}

/**
 * 
 * Visitor to write for each visited node a string like:
 * [<NodeType>,(<line>:<column>),(<lastLine>:<lastColumn>)]
 * 
 */
class LineCheckVisitor extends ClassCodeVisitorSupport {
    
    private StringBuffer astString = new StringBuffer();
    
    public String getASTString() {
        return astString.toString();
    }

    protected void visitStatement(Statement statement) {
        visitNode(statement);
    }
    
    protected void visitType(ClassNode node) {
        visitNode(node);
        visitGenerics(node);
    }
    
    protected void visitTypes(ClassNode[] classNodes) {
        if (classNodes != null) {
            for (ClassNode classNode : classNodes) {
                visitType(classNode);
            }
        }
    }
    
    protected void visitGenerics(ClassNode node) {
        if (node.isUsingGenerics()) {
            GenericsType[] generics = node.getGenericsTypes();
            if(generics == null) return;
            for (GenericsType genericType : generics) {
                visitNode(genericType);
                visitType(genericType.getType());
                if (genericType.getLowerBound() != null) {
                    visitType(genericType.getLowerBound());
                }
                visitTypes(genericType.getUpperBounds());
            }
        }
    }
    
    protected void visitNodes(ASTNode[] nodes) {
        if (nodes != null) {
            for (ASTNode node : nodes) {
                visitNode(node);
            }
        }
    }

    protected void visitNode(ASTNode node) {
        String nodeName = node.getClass().getName();
        //get classname without package
        nodeName = nodeName.substring(nodeName.lastIndexOf(".") + 1,nodeName.length());
        astString.append("[");
        astString.append(nodeName);
        astString.append(",(");
        astString.append(node.getLineNumber());
        astString.append(":");
        astString.append(node.getColumnNumber());
        astString.append("),(");
        astString.append(node.getLastLineNumber());
        astString.append(":");
        astString.append(node.getLastColumnNumber());
        astString.append(")]");
        //String of each node looks like: [AssertStatement,(1:1),(1:20)]
    }

    public SourceUnit getSourceUnit() {
        return null;
    }

    public void visitModuleNode(ModuleNode moduleNode) {
        
        //visit imports like import java.io.File and import java.io.File as MyFile
        for (ImportNode importNode : moduleNode.getImports()) {
            visitNode(importNode.getType());
        }
        
        //visit static imports like import java.lang.Math.*
        for (ImportNode importNode : moduleNode.getStaticStarImports().values()) {
            visitNode(importNode.getType());
        }
        
        //visit static imports like import java.lang.Math.cos
        for (ImportNode importNode : moduleNode.getStaticImports().values()) {
            visitNode(importNode.getType());
        }

        for (ClassNode classNode : moduleNode.getClasses()) {
            if (!classNode.isScript()) {
                visitClass(classNode);
            } else {
                for (MethodNode method : moduleNode.getMethods()) {
                    visitMethod(method);
                }
            }
        }
        //visit Statements that are not inside a class
        if (!moduleNode.getStatementBlock().isEmpty()) {
            visitBlockStatement(moduleNode.getStatementBlock());
        }
    }

    public void visitClass(ClassNode node) {
        visitType(node);
        visitType(node.getUnresolvedSuperClass());
        visitTypes(node.getInterfaces());
        super.visitClass(node);
    }

    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotationMap = node.getAnnotations();
        if (annotationMap.isEmpty()) return;
        visitNode(node);
        for (AnnotationNode annotationNode : annotationMap) {
            visitNode(annotationNode);
        }
    }
    
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        visitAnnotations(node);
        analyseMethodHead(node);
        Statement code = node.getCode();
        
        visitClassCodeContainer(code);
    }
    
    private void analyseMethodHead(MethodNode node) {
        visitNode(node.getReturnType());
        analyseParameters(node.getParameters());
        visitNodes(node.getExceptions());
    }
    
    private void analyseParameters(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            visitType(parameter.getOriginType());
            if (parameter.hasInitialExpression()) {
                parameter.getInitialExpression().visit(this);
            }
        }
    }

    public void visitConstructor(ConstructorNode node) {
        visitNode(node);
        super.visitConstructor(node);
    }

    public void visitMethod(MethodNode node) {
        visitNode(node);
        super.visitMethod(node);
    }

    public void visitField(FieldNode node) {
        // Do not visit fields which are manually added due to optimization
        if (!node.getName().startsWith("$")) {
            visitType(node.getOriginType());
            visitNode(node);
            super.visitField(node);
        }
    }

    public void visitProperty(PropertyNode node) {
        // do nothing, also visited as FieldNode
    }
    
    /*
     * Statements
     * 
     * Statements not written here are visited in ClassCodeVisitorSupport and call there
     * visitStatement(Statement statement) which is overridden in this class
     */

    /*
     * Expressions
     */
    public void visitMethodCallExpression(MethodCallExpression call) {
        visitNode(call);
        super.visitMethodCallExpression(call);
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        visitNode(call);
        super.visitStaticMethodCallExpression(call);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        visitNode(call);
        visitType(call.getType());
        super.visitConstructorCallExpression(call);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        visitNode(expression);
        super.visitBinaryExpression(expression);
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        visitNode(expression);
        super.visitTernaryExpression(expression);
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        visitNode(expression);
        super.visitPostfixExpression(expression);
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        visitNode(expression);
        super.visitPrefixExpression(expression);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        visitNode(expression);
        super.visitBooleanExpression(expression);
    }

    public void visitNotExpression(NotExpression expression) {
        visitNode(expression);
        super.visitNotExpression(expression);
    }

    public void visitClosureExpression(ClosureExpression expression) {
        visitNode(expression);
        super.visitClosureExpression(expression);
    }

    public void visitTupleExpression(TupleExpression expression) {
        visitNode(expression);
        super.visitTupleExpression(expression);
    }

    public void visitListExpression(ListExpression expression) {
        visitNode(expression);
        super.visitListExpression(expression);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        visitNode(expression);
        visitNode(expression.getElementType());
        super.visitArrayExpression(expression);
    }

    public void visitMapExpression(MapExpression expression) {
        visitNode(expression);
        super.visitMapExpression(expression);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        visitNode(expression);
        super.visitMapEntryExpression(expression);
    }

    public void visitRangeExpression(RangeExpression expression) {
        visitNode(expression);
        super.visitRangeExpression(expression);
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        visitNode(expression);
        super.visitSpreadExpression(expression);
    }

    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        visitNode(expression);
        super.visitSpreadMapExpression(expression);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        visitNode(expression);
        super.visitMethodPointerExpression(expression);
    }

    public void visitBitwiseNegationExpression(
            BitwiseNegationExpression expression) {
        visitNode(expression);
        super.visitBitwiseNegationExpression(expression);
    }

    public void visitCastExpression(CastExpression expression) {
        visitNode(expression);
        visitType(expression.getType());
        super.visitCastExpression(expression);
    }

    public void visitConstantExpression(ConstantExpression expression) {
        visitNode(expression);
        super.visitConstantExpression(expression);
    }

    public void visitClassExpression(ClassExpression expression) {
        visitNode(expression);
        super.visitClassExpression(expression);
    }

    public void visitVariableExpression(VariableExpression expression) {
        visitNode(expression);
        super.visitVariableExpression(expression);
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        //visitNode(expression); is visited afterwards in BinaryExpression. Because
        //super.visitDeclarationExpression calls visitBinaryExpression
        visitType(expression.getLeftExpression().getType());
        super.visitDeclarationExpression(expression);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        visitNode(expression);
        super.visitPropertyExpression(expression);
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        visitNode(expression);
        super.visitAttributeExpression(expression);
    }

    public void visitFieldExpression(FieldExpression expression) {
        visitNode(expression);
        super.visitFieldExpression(expression);
    }

    public void visitGStringExpression(GStringExpression expression) {
        visitNode(expression);
        super.visitGStringExpression(expression);
    }

    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        //visitNode(ale); is visited afterwards in TupleExpression. Because
        //super.visitArgumentlistExpression calls visitTupleExpression
        super.visitArgumentlistExpression(ale);
    }

    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitNode(expression);
        super.visitShortTernaryExpression(expression);
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        visitNode(expression);
        super.visitUnaryPlusExpression(expression);
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        visitNode(expression);
        super.visitUnaryMinusExpression(expression);
    }

    public void visitClosureListExpression(ClosureListExpression cle) {
        visitNode(cle);
        super.visitClosureListExpression(cle);
    }
}
