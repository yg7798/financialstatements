package com.tekion.accounting.fs.service.common.parsing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
@Component
@AllArgsConstructor
public class ScriptParser {
	public static final String JAVASCRIPT_ENGINE_NAME = "JavaScript";

	ScriptEngine se;
	public ScriptParser(){
		se = new ScriptEngineManager().getEngineByName(JAVASCRIPT_ENGINE_NAME);
	}

	public Object eval(String s) throws ScriptException {
		return se.eval(s);
	}
}