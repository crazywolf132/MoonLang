/*
 * Copyright 2019 This source file is part of the Máni open source project
 *
 * Copyright (c) 2018 - 2019.
 *
 * Licensed under Mozilla Public License 2.0
 *
 * See https://github.com/mani-language/Mani/blob/master/LICENSE.md for license information.
 */


package com.mani.lang.Modules.logger;

import com.mani.lang.core.Interpreter;
import com.mani.lang.domain.ManiCallableInternal;
import com.mani.lang.Modules.Module;
import com.mani.lang.domain.ManiCallable;
import com.mani.lang.main.Mani;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class logger implements Module {

    @Override
    public void init(Interpreter interpreter) {
        interpreter.addSTD("newLogger", new ManiCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new Logman();
            }
        });
    }

    @Override
    public boolean hasExtensions() {
        return true;
    }

    @Override
    public Object extensions() {
        HashMap<String, HashMap<String, ManiCallableInternal>> db = new HashMap<>();
        HashMap<String, ManiCallableInternal> locals = new HashMap<>();

        locals.put("level", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).setLevel(((Double) arguments.get(0)).intValue());
                return null;
            }
        });
        locals.put("log", new ManiCallableInternal() {
            @Override
            public int arity() { return 2; }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (!(arguments.get(0) instanceof Double && arguments.get(1) instanceof String)) {
                    Mani.printAndStoreError("Arguments must be a Number and String");
                    return null;
                }
                ((Logman) this.workWith).logLevel((String) arguments.get(1), ((Double) arguments.get(0)).intValue());
                return null;
            }
        });
        locals.put("singleLine", new ManiCallableInternal() {
            @Override
            public int arity() { return 2; }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (!(arguments.get(0) instanceof Double && arguments.get(1) instanceof String)) {
                    Mani.printAndStoreError("Arguments must be a Number and String");
                    return null;
                }
                ((Logman) this.workWith).logLevel((String) arguments.get(1), ((Double) arguments.get(0)).intValue(), "\r");
                return null;
            }
        });
        locals.put("write", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (!(arguments.get(0) instanceof String)) {
                    Mani.printAndStoreError("Argument must be a String");
                    return null;
                }
                String result;
                try {
                    File f = new File((String) arguments.get(0));

                    PrintWriter pw = new PrintWriter(f);

                    List<String> toWrite = ((Logman) this.workWith).getRecords();

                    for (String message : toWrite) {
                        pw.write(message);
                    }

                    pw.flush();
                    pw.close();

                    pw = null;
                    f = null;
                    toWrite = null;

                    result = "Finished writing";

                } catch (FileNotFoundException e) {
                    Mani.printAndStoreError("Could not write to file!");
                   return e.getMessage();
                }
                return result;
            }
        });
        locals.put("error", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).logLevel((String) arguments.get(0), 3);
                return null;
            }
        });
        locals.put("warning", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).logLevel((String) arguments.get(0), 2);
                return null;
            }
        });
        locals.put("info", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).logLevel((String) arguments.get(0), 1);
                return null;
            }
        });
        locals.put("dateConfig", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).setDateTime((Boolean) arguments.get(0));
                return null;
            }
        });
        locals.put("setWarning", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).setWarning((String) arguments.get(0));
                return null;
            }
        });
        locals.put("setError", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).setError((String) arguments.get(0));
                return null;
            }
        });
        locals.put("setInfo", new ManiCallableInternal() {
            @Override
            public int arity() { return 1; }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                ((Logman) this.workWith).setInfo((String) arguments.get(0));
                return null;
            }
        });

        db.put("logger", locals);
        return db;
    }



    public static class Logman {
        private int logLevel = -1;
        private int lastLogLevel = 0;
        private String warningNotice = "";
        private String errorNotice = "";
        private String infoNotice = "";
        private String pastHeader = "";
        private boolean recordDateTime = false;
        private List<String> records = new ArrayList<>();

        Logman() {}

        public String log(String message, String prefix) {
            String msg = "";

            if (!prefix.isEmpty()) {
                msg += prefix;
            } else {
                System.out.print("\n");
            }

            if (this.recordDateTime) {
                Date d = new Date();
                msg += "[" + d.getDate() +"/"+ d.getMonth() +"/"+ d.getYear() + " - " + d.getHours() + ":" + d.getMinutes() + "]\t";
            }
            String header = "";
            switch(logLevel) {
                case 2:
                    header += warningNotice;
                    break;
                case 3:
                    header += errorNotice;
                    break;
                case 1:
                default:
                    header += infoNotice;
                    break;
            }

            if (this.lastLogLevel != this.logLevel) {
                msg += header;
                this.pastHeader = header;
            } else if (!this.pastHeader.equals(header)) {
                // This means its the same type, but the header has changed...
                msg += header;
                this.pastHeader = header;
            } else {
                for (int i = 0; i < this.pastHeader.length(); i++) {
                    msg += " ";
                }
            }

            msg += "\t|\t";
            msg += message;

            Mani.printAndStoreError(msg);
            this.records.add(msg + "\n");
            return msg;
        }

        public String logLevel(String message, int level) {
            return this.logLevel(message, level, "");
        }

        public String logLevel(String message, int level, String prefix) {
            this.setLevel(level);
            return this.log(message, prefix);
        }

        public int setLevel(int level) {
            this.lastLogLevel = this.logLevel;
            this.logLevel = level;
            return level;
        }
        public String setWarning(String message) {
            this.warningNotice = message;
            return message;
        }
        public String setError(String message) {
            this.errorNotice = message;
            return message;
        }
        public String setInfo(String message) {
            this.infoNotice = message;
            return message;
        }
        public boolean setDateTime(boolean yesNo) {
            this.recordDateTime = yesNo;
            return yesNo;
        }
        public List<String> getRecords() { return this.records; }


    }
}