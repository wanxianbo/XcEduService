<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
<#--Hello ${name}!-->
<h2>Hello ${name}</h2>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
        <td>生日</td>
    </tr>
    <#list stus as stu>
        <tr>
            <td>${stu_index + 1}</td>
            <td <#if stu.name == '小明'>style="background: blue" </#if>>${stu.name}</td>
            <td>${stu.age}</td>
            <td>${stu.money}</td>
            <td>${(stu.birthday?datetime)!''}</td>
        </tr>
    </#list>
    <#--遍历map-->
</table>
<br>
学生的个数：${stus?size}<br>
输出stu1的学生信息：<br/>
姓名：${stuMap['stu1'].name}<br>
年龄：${stuMap['stu1'].age}<br>
输出stu1的学生信息：<br/>
姓名：${stuMap.stu1.name}<br>
年龄：${stuMap.stu1.age}<br>
遍历输出两个学生信息：<br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
        <td>朋友</td>
    </tr>
    <#list stuMap?keys as key>
        <tr>
            <td>${key_index + 1}</td>
            <td>${stuMap[key].name}</td>
            <td>${stuMap[key].age}</td>
            <td>${stuMap[key].money}</td>
            <td>${(stuMap[key].bestFriend.name)!''}</td>
        </tr>
    </#list>
</table>
${point}
<br>
${point?c}
<br>
<#assign text="{'bank':'工商银行','account':'10101920201920212'}" />
<#assign data=text?eval />
开户行：${data.bank} 账号：${data.account}
</body>
</html>