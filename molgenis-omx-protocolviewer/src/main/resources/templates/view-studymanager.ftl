<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["ui.dynatree.css", "studymanager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "jquery.dynatree.min.js", "studymanager.js"]>
<@header css js/>
	<div class="span8 offset2">
		<div class="row-fluid">	
			<div class="well">
				<p id="loader-title" class="box-title">Choose a study definition to manage</p>
				<form id="studyDefinitionForm" name="studyDefinitionForm" method="post" action="${context_url}/load" onsubmit="parent.showSpinner(); return true;">
					<div class="row-fluid">
						<div class="span6">
							<div class="row-fluid">
								<div class="control-group form-horizontal">
									<label class="control-label" for="state-select">Study definition status:</label>
									<div class="controls">
										<select id="state-select" name="state-select">
										<#list studyDefinitionStates as studyDefinitionState>
											<option value="${studyDefinitionState}"<#if studyDefinitionState == defaultStudyDefinitionState> selected</#if>>${studyDefinitionState}</option>
										</#list> 
										</select>
									</div>
								</div>
							</div>
							<div class="row-fluid">
								<div id="resultsTable">
									<table id="studyDefinitionList" class="table table-striped table-hover listtable selection-table">
										<thead>
											<tr>
												<th></th>
												<th>Id</th>
												<th>Name</th>
												<th>Email</th>
												<th>Date</th>
											</tr>
										</thead>
										<tbody>
										</tbody>
									</table>
								</div>
							<#if dataLoadingEnabled>
								<input type="submit" class="btn pull-right" value="Load" />
							</#if>
							</div>
						</div>
						<div class="span6" id="study-definition-info">
							<ul class="nav nav-tabs">
								<li id='details-tab' class="active"><a href="#study-definition-viewer" data-toggle="tab">Details</a></li>
								<li id='manage-tab' ><a href="#study-definition-editor" data-toggle="tab">Manage</a></li>
							</ul>
							<div class="tab-content">
							    <div class="tab-pane active" id="study-definition-viewer">
									<div id="study-definition-viewer-container">
										<div id="study-definition-viewer-info">
										</div>
										<div id="study-definition-viewer-tree">
										</div>
									</div>
									<button id="download-study-definition-btn" class="btn pull-right" type="button">Download</button>
							    </div>
							    <div class="tab-pane" id="study-definition-editor">
							    	<div id="study-definition-editor-container">
								    	<div id="study-definition-editor-info">
										</div>
                                        <div id="study-definition-state-select">
                                            <select id="edit-state-select" name="edit-state-select">
                                                <#list studyDefinitionStates as studyDefinitionState>
                                                    <option value="${studyDefinitionState}">${studyDefinitionState}</option>
                                                </#list>
                                            </select>
                                        </div>
										<div id="study-definition-editor-tree">
										</div>
									</div>
									<input id="update-study-definition-btn" type="button" class="btn pull-right" value="Save" />
							    </div>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
<@footer/>