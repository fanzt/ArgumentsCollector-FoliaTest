# ArgumentsCollector
English version below

仅用于Minecraft服务器菜单使用，让所需命令成为询问式交互
适合放在菜单中引导玩家使用命令

用法：/ac <命令> <第一个参数的提示> <第二个参数的提示> ... <第X个参数的提示>

若命令或提示中有空格，只需要加上""，例如命令"tell Learting"、提示"请这样输入：1 2 3"


示例（菜单触发指令）：
/ac "tell Learting" "请问你要说什么？"

记录：
[CHAT] ⌌(1/1) 请问你要说什么？
[CHAT] ⌎test
[CHAT] You whisper to Learting: test

结果等效于/tell Learting test

询问中途输入q来退出


English version:

For use on Minecraft server menus only, making the required commands interrogative interactions
Ideal for placing in the menu to guide the player through the commands

Usage: /ac <command> <prompt for the first argument> <prompt for the second argument> ... <prompt for xth argument> ...

If there are spaces in the command or prompt, just add "", e.g. command "tell Learting", prompt "Please enter like this: 1 2 3"


Example (menu triggered command):
/ac "tell Learting" "What would you like to say?"

Record:
[CHAT] ⌌(1/1) What would you like to say?
[CHAT] ⌎test
[CHAT] You whisper to Learting: test

The result is equivalent to /tell Learting test

enter 'q' to exit the process at anytime

