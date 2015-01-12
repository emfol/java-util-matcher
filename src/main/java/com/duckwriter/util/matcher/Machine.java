package com.duckwriter.util.matcher;

class Machine extends Object {

    private static final int CONFIG_STACK_SIZE = 256;

    private static final int OPCODE_HALT   = 0x00; // HLT
    private static final int OPCODE_JUMP   = 0x01; // JMP
    private static final int OPCODE_BRANCH = 0x02; // BL
    private static final int OPCODE_MATCH  = 0x03; // MT

    /*
     * Instance Variables
     */

    private final int[] stack;
    private int[] program;
    private int pc;
    private int sp;
    private int cr;
    private int dx;
    private int st;

    /*
     * Constructors
     */

    Machine() {
        super();
        this.stack = new int[CONFIG_STACK_SIZE];
        this.program = null;
    }

    /*
     * Internal Methods
     */

    private boolean step()
        throws Exception {

        boolean result = true;
        int data, opcode, instruction;

        // load instruction
        if (this.pc >= this.program.length) {
            throw new Exception("Bad Instruction Pointer");
        }
        instruction = this.program[this.pc];

        // decode instruction
        opcode = instruction >>> 16;
        data = instruction & 0xFFFF;

        switch (opcode) {

            case OPCODE_HALT:
                this.st = data;
                result = false;
                break;

            case OPCODE_JUMP:
                this.pc = data;
                break;

            case OPCODE_BRANCH:
                if (this.sp >= CONFIG_STACK_SIZE) {
                    throw new Exception("Stack Overflow Exception");
                }
                this.stack[this.sp++] = this.pc + 1;
                this.pc = data;
                break;

            case OPCODE_MATCH:
                // mode: concat || altern
                if (this.dx != data) {
                    if (this.sp < 1) {
                        throw new Exception("Empty Stack Exception");
                    }
                    this.pc = this.stack[--this.sp];
                }
                break;

            default:
                throw new Exception("Unknown Opcode Exception");
                break;

        }

        return result;

    }

    /*
     * Interface
     */

    void program(final int[] text) {
        this.pc = 0;
        this.sp = 0;
        this.cr = 0;
        this.st = 0;
        this.program = text;
    }

    int state() {
        return this.st;
    }

    boolean execute(final byte[] data, final int length)
        throws Exception {

        boolean result = true;
        int i;

        if (this.program == null
            || this.program.length < 1) {
            throw new Exception("Invalid Program Exception");
        }

        for (i = 0; i < length; ++i) {
            this.dx = (int)data[i];
            if (!this.step()) {
                result = false;
                break;
            }
        }

        return result;

    }

    void reset() {
        this.pc = 0;
        this.sp = 0;
        this.cr = 0;
        this.st = 0;
    }

}
