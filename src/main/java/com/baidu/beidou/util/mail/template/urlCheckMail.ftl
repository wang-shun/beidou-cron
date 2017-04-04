<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head></head>
<body>
尊敬的&nbsp;${realName?html}&nbsp;先生/小姐，您好！<br>
<p>
<br>
　　您的${refuseNumber?html}个创意未通过系统审核，已经下线。为不影响您的消费计划，保证您的创意正常投放，请参考系统拒绝理由及时检查您创意的相应内容，进行修改后重新提交。<br>
</p>
<p>
<table cellpadding="2" cellspacing="2" border="1" bordercolor="#FFFFFF" borderstyle="solid">
	<tr>
		<td align="center" width="20%"><b>推广计划名</b></td>
		<td align="center" width="20%"><b>推广组名</b></td>
		<td align="center" width="20%"><b>下线创意数</b></td>
		<td align="center" width="40%"><b>拒绝理由</b></td>
	</tr>
<#list auditList as info>
	<tr>
		<td align="center">${info.planName?html}</td>
		<td align="center">${info.groupName?html}</td>
		<td align="center">${info.count}</td>
		<td align="center">${info.reason}</td>
	</tr>
</#list>	
</table>
<br>
</p>
<p>
　　您可登录系统察看详细情况。<br>
　　如有问题，请参看百度网盟推广服务常见问题解答，网址： <a href="http://support.baidu.com/wm/">http://support.baidu.com/wm/</a><br>
<br>
</p>
<p>
　　谢谢您选择我们的服务！<br>
<br>
</p>
<p>
注：此邮件为系统自动发送，请勿回复。<br>
此致 <br>
　　敬礼<br>
--<br>
百度公司 <br>
电话: 400-890-0088<br>
传真: 010-59222966<br>
E-mail: nrhelp@baidu.com<br>
</p>
</body>
</html>
