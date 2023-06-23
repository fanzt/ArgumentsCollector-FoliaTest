# ArgumentsCollector
[简体中文](#简体中文) / [English](#english)

## 简体中文
用于Minecraft服务器使用，让所需命令成为询问式交互。适合放在菜单中引导玩家使用命令。

### 使用方法：
```
/ac (-f) <命令> <第一个参数的提示> <第二个参数的提示> ... <第X个参数的提示>
```

- 若命令或提示中有空格，只需要加上`""`，例如命令`"tell Learting"`、提示`"请这样输入：1 2 3"`
- 查询中途输入`q`来退出。

### 示例（菜单触发指令）：
```
/ac "tell Learting" "请问你要说什么？"
```

#### 记录：
```
[Server] ⌌(1/1) 请问你要说什么？
[Player] ⌎test
[Server] You whisper to Learting: test
```

结果等效于`/tell Learting test`。

### 格式化&PlaceHolderAPI示例（菜单触发指令）：
（玩家站在领地`TestResidence`内）
```
/ac -f "tell %s 我在%residence_user_current_res%里" "请问你想和谁说？" 
```

#### 记录：
```
[Server] ⌌(1/1) 请问你想和谁说？
[Player] ⌎Learting
[Server] [我 -> Learting] 你在TestResidence里
```

---

## English
Intended for use on Minecraft server menus to make the required commands interrogative interactions. Ideal for placing in the menu to guide the player through the commands.

### Usage:
```
/ac (-f) <command> <prompt for the first argument> <prompt for the second argument> ... <prompt for xth argument>
```

- If there are spaces in the command or prompt, just add "", e.g. command `"tell Learting"`, prompt `"Please enter like this: 1 2 3"`
- Enter `q` to exit the process at anytime.

### Example (menu triggered command):
```
/ac "tell Learting" "What would you like to say?"
```

#### Log:
```
[Server] ⌌(1/1) What would you like to say?
[Player] ⌎test
[Server] You whisper to Learting: test
```

The result is equivalent to `/tell Learting test`.

### Example with Formatting and PlaceHolderAPI(menu triggered command):
(Player is in residence `TestResidence`)
```
/ac -f "tell %s I am now in %residence_user_current_res%" "Who would you want to talk to?"
```

#### Log:
```
[Server] ⌌(1/1) "Who would you want to talk to?"
[Player] ⌎Learting
[Server] [Me -> Learting] I am now in TestResidence
```
