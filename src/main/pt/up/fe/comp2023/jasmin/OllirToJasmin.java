package pt.up.fe.comp2023.jasmin;

import pt.up.fe.specs.util.exceptions.NotImplementedException;
import org.specs.comp.ollir.*;
import java.util.Map;

public class OllirToJasmin {

    int limitlocal = 0;
    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public String getCode() {
        var code = new StringBuilder();

        code.append(".class public ").append(classUnit.getClassName()).append("\n");
        code.append(".super java/lang/Object\n");

        for (var fields : classUnit.getFields()) {
            code.append(getFields((Field) fields));
        }

        code.append("\n");

        for (var methods : classUnit.getMethods()) {
            if (methods.isConstructMethod()) {
                code.append(getConstructCode(methods));
            } else {
                code.append(getCode(methods));
            }
        }

        return code.toString();
    }

    // Constructor Code
    public String getConstructCode(Method method) {
        var code = new StringBuilder();
        var jasminConstructor = ".method public <init>()V\naload_0\ninvokespecial java/lang/Object/<init>()V\nreturn\n.end method\n\n";

        code.append(jasminConstructor);

        return code.toString();
    }

    // Fields
    public String getFields(Field field) {
        var code = new StringBuilder();

        code.append(".field ");

        if (field.isStaticField()) {
            code.append("static ");
        } else if (field.isFinalField()) {
            code.append("final ");
        }

        if (!field.getFieldAccessModifier().name().equals("DEFAULT")) {
            code.append(field.getFieldAccessModifier().name().toLowerCase()).append(" ");
        }

        code.append(field.getFieldName() + " " + getJasminType(field.getFieldType()) + "\n");

        return code.toString();
    }

    // Methods
    public String getCode(Method method) {
        var code = new StringBuilder();

        code.append(".method ");

        if (!method.getMethodAccessModifier().name().equals("DEFAULT")) {
            code.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        }

        if (method.isStaticMethod()) {
            code.append("static ");
        } else if (method.isFinalMethod()) {
            code.append("final ");
        }

        code.append(method.getMethodName()).append("(");

        for (var parameters : method.getParams()) {
            code.append(getJasminType(parameters.getType()));
        }

        code.append(")" + getJasminType(method.getReturnType())).append("\n");

        String stack = ".limit stack 99\n";
        String locals = ".limit locals 99\n";

        var code2 = new StringBuilder();

        for (var inst : method.getInstructions()) {
            code2.append(getCode(inst, method));
        }

        code.append(stack);
        code.append(locals);

        code.append(code2);
        code.append(".end method\n\n");

        return code.toString();
    }

    // Instructions
    public String getCode(Instruction instruction, Method method) {
        switch (instruction.getInstType()) {
            case CALL:
                return getCode((CallInstruction) instruction, method);
            case GETFIELD:
                return getCode((GetFieldInstruction) instruction, method);
            case PUTFIELD:
                return getCode((PutFieldInstruction) instruction, method);
            case ASSIGN:
                return getCode((AssignInstruction) instruction, method);
            case RETURN:
                return getCode((ReturnInstruction) instruction, method);
            case UNARYOPER:
            case BINARYOPER:
                return getCode((BinaryOpInstruction) instruction, method);
            default:
                break;
        }
        throw new NotImplementedException(instruction.getInstType());
    }

    // Call of Instructions Invoke
    public String getCode(CallInstruction callInstruction, Method method) {
        switch (callInstruction.getInvocationType()) {
            case invokestatic:
                return callInvoke(callInstruction, "static", method);
            case invokespecial:
                return callInvoke(callInstruction, "special", method);
            case invokevirtual:
                return callInvoke(callInstruction, "virtual", method);
            case NEW:
                return getNewCall(callInstruction, method);
            default:
                break;
        }
        throw new NotImplementedException(callInstruction.getInvocationType());
    }

    // Invoke
    public String callInvoke(CallInstruction callInstruction, String type, Method method) {
        var callInvoke = ((Operand) callInstruction.getFirstArg()).getName();
        var code = new StringBuilder();

        if (type == "static") {
            for (var element : callInstruction.getListOfOperands()) {
                if (element.isLiteral()) {
                    code.append(ldcPush(element));
                } else {
                    code.append(getTypeInfo(element.getType().getTypeOfElement()) + "load_" + getRegisterIndex(method, ((Operand) element).getName().toString()) + "\n");
                }
            }

            code.append(callInstruction.getInvocationType() + " ");
            code.append(callInvoke);
            code.append(auxCallInvoke(callInstruction));

        } else if (type == "special") {
            code.append(callInstruction.getInvocationType() + " ");
            code.append(classUnit.getClassName());
            code.append(auxCallInvoke(callInstruction));

            if (getRegisterIndex(method, callInvoke) != -1) {
                code.append(getTypeInfo(callInstruction.getFirstArg().getType().getTypeOfElement()) + "store_" + getRegisterIndex(method, callInvoke) + "\n");
            }
        } else if (type == "virtual") {
            if (getRegisterIndex(method, callInvoke) != -1) {
                code.append(getTypeInfo(callInstruction.getFirstArg().getType().getTypeOfElement()) + "load_" + getRegisterIndex(method, callInvoke) + "\n");
            }

            code.append(callInstruction.getInvocationType() + " ");
            code.append(classUnit.getClassName());
            code.append(auxCallInvoke(callInstruction));
        }
        return code.toString();
    }

    // Constants
    public String ldcPush(Element element) {
        var code = new StringBuilder();
        limitlocal++;

        code.append("ldc " + ((LiteralElement) element).getLiteral() + "\n");

        return code.toString();
    }

    private String getArgumentCode(Element element) {
        var code = new StringBuilder();

        return code.append(getJasminType(element.getType().getTypeOfElement())).toString();
    }

    // Call Body Invoke
    private String auxCallInvoke(CallInstruction callInstruction) {
        var code = new StringBuilder();
        code.append("/");
        var literal = ((LiteralElement) callInstruction.getSecondArg()).getLiteral();
        code.append(literal.replace("\"", ""));
        code.append("(");

        for (var element : callInstruction.getListOfOperands()) {
            code.append(getArgumentCode(element));
        }

        code.append(")");
        code.append(getJasminType(callInstruction.getReturnType()));
        code.append("\n");
        return code.toString();
    }

    // New Call Instruction (to pass test "ollirToJasminInvoke")
    public String getNewCall(CallInstruction callInstruction, Method method) {
        var code = new StringBuilder();

        switch (callInstruction.getFirstArg().getType().getTypeOfElement()) {
            case ARRAYREF:
                code.append("iload_1\nnewarray int\n");
                break;
            default:
                code.append("new " + this.classUnit.getClassName() + "\ndup\n");
                break;
        }

        return code.toString();
    }

    // Get Code for getFieldInstruction
    public String getCode(GetFieldInstruction getFieldInstruction, Method method) {
        var code = new StringBuilder();

        code.append("aload_0\n");
        code.append("getfield ");
        code.append(this.classUnit.getClassName() + "/" + ((Operand) ((Element) getFieldInstruction.getSecondOperand())).getName() + " " + getJasminType(((Element) getFieldInstruction.getSecondOperand()).getType()) + "\n");

        return code.toString();
    }

    // Get Code for putFieldInstruction
    public String getCode(PutFieldInstruction putFieldInstruction, Method method) {
        var code = new StringBuilder();
        var thirdElement = (Element) putFieldInstruction.getThirdOperand();

        code.append("aload_0\n");
        code.append(pushIntoStack(thirdElement));
        code.append("putfield ");
        code.append(this.classUnit.getClassName() + "/" + ((Operand) ((Element) putFieldInstruction.getSecondOperand())).getName() + " " + getJasminType(((Element) putFieldInstruction.getSecondOperand()).getType()) + "\n");

        return code.toString();
    }

    // Get Code for assignInstruction
    public String getCode(AssignInstruction assignInstruction, Method method) {
        var code = new StringBuilder();
        var operand = (Operand) assignInstruction.getDest();

        if (assignInstruction.getRhs().getInstType() == InstructionType.NOPER) {
            var instruction1 = (Instruction) assignInstruction.getRhs();
            var singleOperand = (SingleOpInstruction) instruction1;
            var element = (Element) singleOperand.getSingleOperand();

            if (element.isLiteral()) {
                code.append(pushIntoStack(element));
                code.append(storeInStack(element, method, operand));
            } else {
                var operand1 = (Operand) singleOperand.getSingleOperand();
                code.append(getTypeInfo(operand1.getType().getTypeOfElement()) + "load_" + getRegisterIndex(method, operand1.getName()) + "\n");
            }

        } else if (assignInstruction.getRhs().getInstType() == InstructionType.BINARYOPER) {
            var operation = ((BinaryOpInstruction) assignInstruction.getRhs()).getOperation();

            code.append(getCode(assignInstruction.getRhs(), method));
            code.append(getTypeInfo(operation.getTypeInfo().getTypeOfElement()) + "store_" + getRegisterIndex(method, operand.getName()) + "\n");

        } else if (assignInstruction.getRhs().getInstType() == InstructionType.CALL) {
            code.append(getCode(assignInstruction.getRhs(), method));
            CallInstruction inst = (CallInstruction) assignInstruction.getRhs();

            if (inst.getInvocationType() == CallType.NEW && ((CallInstruction) inst).getFirstArg().getType().getTypeOfElement() == ElementType.ARRAYREF) {
                code.append("astore_" + getRegisterIndex(method, operand.getName()) + "\n");
            }
        } else {
            code.append(getCode(assignInstruction.getRhs(), method));
        }

        return code.toString();
    }

    public String pushIntoStack(Element element) {
        var code = new StringBuilder();

        switch (element.getType().getTypeOfElement()) {
            case INT32:
            case BOOLEAN:
                if (Integer.parseInt(((LiteralElement) element).getLiteral()) < 6) {
                    code.append("iconst_");
                } else {
                    code.append("sipush ");
                }
                code.append(((LiteralElement) element).getLiteral() + "\n");
                break;
            default:
                break;
        }
        return code.toString();
    }

    public String storeInStack(Element element, Method method, Operand operand) {
        var code = new StringBuilder();

        code.append(getTypeInfo(element.getType().getTypeOfElement()));
        code.append("store_" + Integer.toString(getRegisterIndex(method, operand.getName())) + "\n");

        return code.toString();
    }

    // Get Code for returnInstruction
    public String getCode(ReturnInstruction returnInstruction, Method method) {
        var code = new StringBuilder();

        if (returnInstruction.hasReturnValue()) {
            var element = (Element) returnInstruction.getOperand();

            if (element.isLiteral()) {
                var literal = ((LiteralElement) element).getLiteral();
                code.append("iconst_" + literal + "\n");
            } else {
                code.append(getTypeInfo(element.getType().getTypeOfElement()) + "load_" + getRegisterIndex(method, ((Operand) element).getName()) + "\n");
            }
        }

        code.append(getTypeInfo(returnInstruction.getElementType()));
        code.append("return \n");

        return code.toString();
    }

    // Get Code for Binary Operations Instruction
    public String getCode(BinaryOpInstruction binaryOpInstruction, Method method) {
        var code = new StringBuilder();
        var operation = binaryOpInstruction.getOperation();


        switch (operation.getOpType()) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                if (binaryOpInstruction.getLeftOperand().isLiteral()) {
                    code.append("ldc " + ((LiteralElement) binaryOpInstruction.getLeftOperand()).getLiteral() + "\n");
                } else {
                    var left = (Operand) binaryOpInstruction.getLeftOperand();
                    code.append(getTypeInfo(((Element) left).getType().getTypeOfElement()) + "load_" + getRegisterIndex(method, left.getName().toString()) + "\n");
                }

                if (binaryOpInstruction.getRightOperand().isLiteral()) {
                    code.append("ldc " + ((LiteralElement) binaryOpInstruction.getRightOperand()).getLiteral() + "\n");
                } else {
                    var right = (Operand) binaryOpInstruction.getRightOperand();
                    code.append(getTypeInfo(((Element) right).getType().getTypeOfElement()) + "load_" + getRegisterIndex(method, right.getName().toString()) + "\n");
                }

                code.append(getTypeInfo(operation.getTypeInfo().getTypeOfElement()));
                code.append(operation.getOpType().toString().toLowerCase() + "\n");
                break;
            default:
                break;
        }
        return code.toString();
    }

    public String getTypeInfo(ElementType elementType) {
        var code = new StringBuilder();

        switch (elementType) {
            case INT32:
            case BOOLEAN:
                code.append("i");
                break;
            case OBJECTREF:
                code.append("a");
                break;
            case ARRAYREF:
                code.append("ia");
                break;
            default:
                break;
        }
        return code.toString();
    }

    public int getRegisterIndex(Method method, String name) {
        var varTable = method.getVarTable();

        for (Map.Entry<String, Descriptor> entry : varTable.entrySet()) {
            String key = entry.getKey();
            Descriptor d1 = entry.getValue();

            if (key.equals(name)) {
                return d1.getVirtualReg();
            }
        }

        return -1;
    }

    public String getJasminType(Type type) {

        if (type instanceof ArrayType) {
            return "[" + getJasminType(((ArrayType) type).getArrayType());
        }

        return getJasminType(type.getTypeOfElement());
    }

    public String getJasminType(ElementType elementType) throws NotImplementedException {
        switch (elementType) {
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case THIS:
            case OBJECTREF:
                return "this";
            default:
                throw new NotImplementedException(elementType);
        }
    }
}
