<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['metadataexplorer.css']>
<#assign js=['metadataexplorer.js', 'jquery.bootstrap.pager.js']>
<@header css js/>
	
<h1>Entities</h1>	
<div class="subtitle">This is a listing of all datasets available in ${molgenis_ui.title?html}</div>

<div class="row-fluid">
	<form id="model-search-form" action="" method="POST">
		<div class="input-append span4">
			<span class="search-label">Search:</span>
			<input class="span8" id="search-input" name="searchTerm" type="text" placeholder="Type here your search" autofocus="autofocus" value="${form.searchTerm!}">
			<button class="btn" type="button" id="clear-button"><i class="icon-large icon-remove"></i></button>
			<button class="btn" type="submit" id="search-button"><i class="icon-large icon-search"></i></button>
		</div>	
		<div class="span8">
			<span class="search-label">Entity types:</span>
			<input type="hidden" value="on" name="_entityClassTypes"/><#-- This is a marker for spring that all checkboxes are deselected -->
			<#list entityClassTypes as type>
				<label><input type="checkbox" name="entityClassTypes" value="${type}" <#if form.entityClassTypes?? && form.entityClassTypes?seq_contains(type)>CHECKED</#if> /> ${type}</label>
			</#list>
		</div>
		<input type="hidden" name="page" value="${form.page}" />
	</form>
</div>			
					
<div id="pager"></div>
					
<#list entityClasses as entityClass>
	<div class="well">
		<div class="row-fluid entity-class-header">
			<h3>${entityClass.fullName}</h3> <i>(${entityClass.entityClassIdentifier})</i>
		</div>
		<div class="row-fluid">
			<div class="span2">Type:</div>
			<div class="span10">${entityClass.type}</div>
		</div>
		<#if entityClass.description?? && entityClass.description != ''>
			<div class="row-fluid">
				<div class="span2">Description:</div>
				<div class="span10">${limit(entityClass.description, 150)}</div>
			</div>
		</#if>
		<#if entityClass.tags?size &gt; 0>
			<div class="row-fluid">
				<div class="span2">Tags:</div>
				<div class="span10">
					<#list entityClass.tags as tag>
						${tag.name}<#if tag != entityClass.tags?last>,</#if>
					</#list>
				</div>
			</div>
		</#if>
		<#if entityClass.homepage?? && entityClass.homepage != ''>
			<div class="row-fluid">
				<div class="span2">Homepage:</div>
				<div class="span10"><a href="${entityClass.homepage}" target="_blank">${entityClass.homepage}</a></div>
			</div>
		</#if>
	</div>
</#list>

<script>
	var nrItems = ${nrItems};
	var nrItemsPerPage = ${nrItemsPerPage};
</script>
<@footer/>