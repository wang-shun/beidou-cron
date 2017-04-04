<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<style>
table{
        font-size:11pt;
        border-left:1px solid gray;
        border-top:1px solid gray;
        border-collapse:collapse;
}
td{
        vertical-align: middle;
        text-align:center;
        border-left:0;
        border-top:0;
        font-size:9pt;
        border-right:1px solid gray;
        border-bottom:1px solid gray;
        border-top:1px solid gray;
        border-left:1px solid gray;
        height:25px;
        padding:0 12px 0 12px;
        border-collapse:collapse;
        word-break : break-all;
}
th{
        vertical-align: middle;
        text-align:center;       
        border-right:1px solid gray;
        border-bottom:1px solid gray;
        border-top:1px solid gray;
        border-left:1px solid gray;
        height:25px;
        padding:0 12px 0 12px;
        border-collapse:collapse;
        word-break : break-all;
}
</style>
</head>
<body>
<table>
	<thead>
		<tr>
			<th>序列号</th>
			<th>用户ID</th>
			<th>用户名</th>
			<th>推广计划名称</th>
			<th>重复的推广组数</th>
			<th>推广组名称</th>
		</tr>
	</thead>
	<tbody>
		<#assign n = 0 />
		<#list repeateInfos as repeateInfo>
			<#assign n = n+1 />
			<tr>
				<td>${n}</td>
				<td>${repeateInfo.userId?html}</td>
				<td>${repeateInfo.userName?html}</td>
				<td>${repeateInfo.planName?html}</td>
				<td>${repeateInfo.repeateGroupNum?html}</td>
				<td>${repeateInfo.repeateGroupNames?html}</td>
			</tr>
		</#list>
	</tbody>
</table>
</body>
</html>