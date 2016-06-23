/*
 *    Copyright 2010-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

// Rhino has println(), but Nashorn does not.
// Both has print(), but Nashorn's outputs newline.
if (typeof println == 'undefined')
	this.println = print;

function printVars(arg1, arg2) {
  println('FUNCTION_' + global_var + '_' + local_var1 + '_' + local_var2 + '_' + arg1 + '_' + arg2);
  println('SCRIPT_VAR=' + ++SCRIPT_VAR);
  println('');

  // to verify 'change' object is just a clone
  hookContext.getChange().setDescription('bogus description');

  local_var1 = 'This overwrites the variable defined in env file, '
    + 'but should be reset on next invocation.'
}

var dog = new Object();
dog.bark = function(arg1, arg2) {
  println('METHOD_' + global_var + '_' + local_var1 + '_' + local_var2 + '_' + arg1 + '_' + arg2);
  println('SCRIPT_VAR=' + ++SCRIPT_VAR);
  println('');
}
