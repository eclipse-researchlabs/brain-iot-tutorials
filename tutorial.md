---
layout: toc-page
title: Tutorials 
description: Tutorials 
permalink: /tutorial/
order: 1
---

These tutorials are step by step recipes which focus on creating and running BRAIN IoT Smart Behaviours. The tutorials aim to be quick to complete and describe actions rather than background detail.

Alhough virtually all work in BRAIN IoT can be done through an IDE, the tutorials use a *shell first* approach so that you can choose the IDE you want to use.

A basic familiarity with Java, Git and Maven is assumed.

<br>
<hr>
<style>
table, td, th {
    text-align: left;
}

table {
    width: 100%;
}
        
th {
    padding: 15px;
    color: Black;
}
td {
    padding 10px;
    color: Black;
}
</style>

<div>
<table>

{% for tutorial in site.tutorial %}<tr><td><a href="{{tutorial.url}}">{{tutorial.title}}</a></td><td>{{tutorial.summary}}</td></tr>
{% endfor %}

</table>
</div>


---
