#### {{name}}
```
{{desc}}
```

###### 请求说明

 	http请求方式: post
	url: {{path}}

###### 参数说明

|参数|参数类型|是否必须|说明
| :- | :- | :- | :- |
{{parameters}}


###### 返回说明
正确的返回示例
```
	{
	"statusCode": 0,
	"message": "成功",
	"content": {
			"id": "0d5b685b2b792fa3e254e1367424eadbc2b761bc3b97975983ba0ddccd659fb3",
			"username": "战马",
			"headerurl": "http://thirdqq.qlogo.cn/qqapp/101469935/4A289A36AFD8B47188C9CAEE80084F75/40"
		}
	}
```
错误的返回示例
```
{
	"statusCode": 300,
	"message": "未找到该用户!"
}
```
###### 返回的content字段说明

|参数|是否必须|说明
| :- | :- | :- |
|id | 是| 用户id |
| username | 是 | 用户名 |
| headerurl | 否 | 用户头像地址 |
