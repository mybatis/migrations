/*
 *    Copyright 2010-2018 the original author or authors.
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

function validateDesc() {
    if (hookContext.getDescription().matches('.*JIRA-[0-9]+.*')) {
        println('Description is valid.');
        println('');
    } else {
        throw 'Description must contain JIRA ticket number.';
    }
}

function renameFile() {
    var oldName = hookContext.getFilename();
    var newName = oldName.replace("JIRA-", "JIRA");
    var scriptsDir = migrationPaths.getScriptPath();
    var src = new java.io.File(scriptsDir, oldName);
    var dest = new java.io.File(scriptsDir, newName);
    if (src.renameTo(dest)) {
        println('Renamed ' + oldName + ' to ' + newName);
        println('');
    }
}
