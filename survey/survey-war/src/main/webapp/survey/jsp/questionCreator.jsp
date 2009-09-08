<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.oreilly.servlet.multipart.*"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>

<%@ include file="checkSurvey.jsp" %>

<%!
  void displaySurveyHeader(QuestionContainerDetail surveyDetail, ResourcesWrapper resources, SurveySessionController surveyScc, JspWriter out, GraphicElementFactory gef) throws SurveyException {
    try{
          QuestionContainerHeader surveyHeader = surveyDetail.getHeader();
          String title = Encode.javaStringToHtmlString(surveyHeader.getTitle());
          String creationDate = resources.getOutputDate(new Date());
          String beginDate = "&nbsp;";
          if (surveyHeader.getBeginDate() != null)
              beginDate = resources.getOutputDate(surveyHeader.getBeginDate());
          String endDate = "&nbsp;";
          if (surveyHeader.getEndDate() != null)
              endDate = resources.getOutputDate(surveyHeader.getEndDate());
          String nbQuestions = "&nbsp;";
          if (surveyHeader.getNbQuestionsPerPage() != 0)
              nbQuestions = new Integer(surveyHeader.getNbQuestionsPerPage()).toString();
          out.println("<center>");
          Board board = gef.getBoard();
          out.println(board.printBefore());
          out.println("<table width=\"100%\">");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("GML.name")+" :</td><td align=left width=\"50%\">"+title+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationDate")+" :</td><td align=left width=\"50%\">"+creationDate+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationBeginDate")+" :</td><td align=left width=\"50%\">"+beginDate+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationEndDate")+" :</td><td align=left width=\"50%\">"+endDate+"</td></tr>");
          out.println("<tr><td class=\"textePetitBold\" align=left width=\"50%\">"+resources.getString("SurveyCreationNbQuestionPerPage")+" :</td><td align=left width=\"50%\">"+nbQuestions+"</td></tr>");
          out.println("</table>");
           out.println("</center>");
          out.println(board.printAfter());
    }
    catch( Exception e){
        throw new  SurveyException("questionCreator_JSP.displaySurveyHeader",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_SURVEY_HEADER",e);           
    }

  }

boolean isCorrectFile(FilePart filePart) {
    String fileName = filePart.getFileName();
    boolean correctFile = false;
    if (fileName != null) {
        String logicalName = fileName.trim();
        if (logicalName != null) {
            if ((logicalName.length() >= 3) && (logicalName.indexOf("*") == -1) && (logicalName.indexOf(".") != -1)) {
                String type = logicalName.substring(logicalName.indexOf(".")+1, logicalName.length());
                if (type.length() >= 3)
                    correctFile = true;
            }
        }
    }
    return correctFile;
}
%>

<% 
//R�cup�ration des param�tres
String action = "";
String question = "";
String nbAnswers = "";
String answerInput = "";
String suggestionAllowed = "";
String suggestionCheck = "";
String suggestion = "";
//String qcmCheck = "";
//String qcm = "0";
//String openQuestionCheck = "";
//String openQuestion = "0";
String nextAction = "";
String style = "";

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
String px =  m_context + "/util/icons/colorPix/1px.gif";

ResourceLocator surveySettings = new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", surveyScc.getLanguage());

String nbMaxAnswers = surveySettings.getString("NbMaxAnswers");

Button validateButton = null;
Button cancelButton = null;
Button finishButton = null;
ButtonPane buttonPane = null;

QuestionContainerDetail survey = null;

SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);
Part part;
File dir = null;
String logicalName = null;
String type = null;
String physicalName = null;
String mimeType = null;
boolean file = false;
long size = 0;
int nb = 0;
int attachmentSuffix = 0;
ArrayList imageList = new ArrayList();
ArrayList answers = new ArrayList();
Answer answer = null;
while ((part = mp.readNextPart()) != null) {
  String mpName = part.getName();
  if (part.isParam()) 
  {
    // it's a parameter part
    SilverpeasParamPart paramPart = (SilverpeasParamPart) part;

    if (mpName.equals("Action"))
        action = paramPart.getStringValue();
    else if (mpName.equals("question"))
        question = paramPart.getStringValue();
    else if (mpName.equals("nbAnswers"))
        nbAnswers = paramPart.getStringValue();
    else if (mpName.equals("SuggestionAllowed"))
        suggestion = paramPart.getStringValue();
    // traitement du choix du style de la question
    else if (mpName.equals("questionStyle"))
    	style = paramPart.getStringValue();
    //else if (mpName.equals("qcm"))
        //qcm = paramPart.getStringValue();
    //else if (mpName.equals("openQuestion")) 
        //openQuestion = paramPart.getStringValue();
    else if (mpName.startsWith("answer")) {
        answerInput = paramPart.getStringValue();
        answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, false, null);
        answers.add(answer);    
    } else if (mpName.equals("suggestionLabel")) {
        answerInput = paramPart.getStringValue();
        answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, true, null);
        answers.add(answer);
    }
    else if (mpName.startsWith("valueImageGallery")) 
    {
    	if (StringUtil.isDefined(paramPart.getStringValue()))
    	{
    		// traiter les images venant de la gallery si pas d'image externe
    		if (!file)
    			answer.setImage(paramPart.getStringValue());
    	}
    }     
  } 
  else if (part.isFile()) 
  {
    // it's a file part
    FilePart filePart = (FilePart) part;
    boolean correctFile = isCorrectFile(filePart);
    if (correctFile) {
      // the part actually contained a file
      logicalName = filePart.getFileName();
      type = logicalName.substring(logicalName.indexOf(".")+1, logicalName.length());
      physicalName = new Long(new Date().getTime()).toString() + attachmentSuffix + "." +type;
      attachmentSuffix = attachmentSuffix + 1;
      mimeType = filePart.getContentType();
      dir = new File(FileRepositoryManager.getAbsolutePath(surveyScc.getSpaceId(), surveyScc.getComponentId())+surveySettings.getString("imagesSubDirectory")+File.separator+physicalName);

      size = filePart.writeTo(dir);
      if (size > 0)
      {
          answer.setImage(physicalName);
          file = true;
      }
    } else { 
      // the field did not contain a file
      file = false;
    }
    out.flush();
  }
}

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<%out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript1.2">
function sendData() {
    if (isCorrectForm()) {
        if (checkAnswers()) {
            if (document.surveyForm.suggestion.checked)
            {
                document.surveyForm.SuggestionAllowed.value = "1";
            }
            document.surveyForm.submit();
        }
    }
}

function checkAnswers() {
     var errorMsg = "";
     var errorNb = 0;
     var answerEmpty = false;
     var imageEmpty = false;
     var fieldsEmpty = "";
     for (var i=0; i<document.surveyForm.length; i++) 
     {
        inputName = document.surveyForm.elements[i].name.substring(0, 5);
        if (inputName == "answe" ) {
            if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
                  answerEmpty = true;
            }
        }
        
        if (inputName == "image") 
        {
            if (answerEmpty == true) {
                  if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
                        imageEmpty = true;
                  }
            }
            answerEmpty = false;
        }
        
        if (inputName == "value") 
        {
            if (imageEmpty == true) {
                  if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
                        fieldsEmpty += (parseInt(document.surveyForm.elements[i].name.substring(17, document.surveyForm.elements[i].name.length))+1)+",";
                        errorNb++;
                  }
            }
            imageEmpty = false;
        }
      }
	 if(<%=!suggestion.equals("0") && action.equals("SendQuestionForm")%>){
         if (isWhitespace(stripInitialWhitespace(document.surveyForm.suggestionLabel.value))) {
                errorNb++;
         }
     }
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        default :
            fields = fieldsEmpty.split(",");
            for (var i=0; i < fields.length-1; i++) {
                errorMsg += "<%=resources.getString("SurveyCreationAnswerNb")%> "+fields[i]+" \n";
            }
			if(<%=!suggestion.equals("0") && action.equals("SendQuestionForm")%>){
                if (isWhitespace(stripInitialWhitespace(document.surveyForm.suggestionLabel.value))) {
                    errorMsg += "<%=resources.getString("OtherAnswer")%> \n";
                }
            }
            window.alert("<%=resources.getString("EmptyAnswerNotAllowed")%> \n" + errorMsg);
            result = false;
            break;
     }
     return result;
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var question = stripInitialWhitespace(document.surveyForm.question.value);
     var nbAnswers = document.surveyForm.nbAnswers.value;
     if (isWhitespace(question)) 
     {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationQuestion")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
     }
     
     if (document.surveyForm.questionStyle.options[document.surveyForm.questionStyle.selectedIndex].value=="null") {
     	//choisir au moins un style
	    	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("survey.style")%>' <%=resources.getString("GML.MustBeFilled")%> \n";
	    	errorNb++;
     }
     else
     {
     	if (document.surveyForm.questionStyle.options[document.surveyForm.questionStyle.selectedIndex].value!="open") {
	          //Closed Question
	          if (isWhitespace(nbAnswers)) {
	             errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
	             errorNb++;
	          } else {
	                 if (isInteger(nbAnswers)==false) {
	                     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
	                     errorNb++;
	                 } else {
	                      if (document.surveyForm.suggestion.checked) {
	                          //nb min answers = 1
	                          if (nbAnswers <= 0) {
	                             errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberGreaterThan")%> 1\n";
	                             errorNb++;
	                          }
	                      } else {
	                          //nb min answers = 2
	                          if (nbAnswers <= 1) {
	                             errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberGreaterThan")%> 2\n";
	                             errorNb++;
	                          }
	                      }
	                      if (nbAnswers > <%=nbMaxAnswers%>) {
	                         errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsNumberLessThan")%> <%=nbMaxAnswers%>\n";
	                         errorNb++;
	                      }
	                 }
	          } 
	     } else {
	          document.surveyForm.Action.value = "SendNewQuestion";
	     }
	 }    
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function goToEnd() {
    document.surveyForm.Action.value = "End";
    document.surveyForm.submit();
}

	var galleryWindow = window;
	var currentAnswer;
	
	function choixGallery(liste, idAnswer)
	{
		currentAnswer = idAnswer;
		index = liste.selectedIndex;
		var componentId = liste.options[index].value;
		if (index != 0)
		{
			url = "<%=m_context%>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=<%=surveyScc.getLanguage()%>";
			windowName = "galleryWindow";
			larg = "820";
			haut = "600";
			windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
			if (!galleryWindow.closed && galleryWindow.name=="galleryWindow")
			{
				galleryWindow.close();
			}
			galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
		}
	}
	
	function deleteImage(idImage)
	{
		document.getElementById('imageGallery'+idImage).innerHTML = "";
		document.getElementById('valueImageGallery'+idImage).value = "";
	}
	
	function choixImageInGallery(url)
	{
		var newLink = document.createElement("a");
		newLink.setAttribute("href", url);
		newLink.setAttribute("target", "_blank");
		
		var newLabel = document.createTextNode("<%=resources.getString("survey.imageGallery")%>");
		newLink.appendChild(newLabel);
		
		var removeLink =  document.createElement("a");
		removeLink.setAttribute("href", "javascript:deleteImage('"+currentAnswer+"')");
		var removeIcon = document.createElement("img");
		removeIcon.setAttribute("src", "icons/questionDelete.gif");
		removeIcon.setAttribute("border", "0");
		removeIcon.setAttribute("align", "absmiddle");
		removeIcon.setAttribute("alt", "<%=resources.getString("GML.delete")%>");
		removeIcon.setAttribute("title", "<%=resources.getString("GML.delete")%>");
		
		removeLink.appendChild(removeIcon);
		
		document.getElementById('imageGallery'+currentAnswer).appendChild(newLink);
		document.getElementById('imageGallery'+currentAnswer).appendChild(removeLink);
		   
		document.getElementById('valueImageGallery'+currentAnswer).value = url;
	}
	
	function showQuestionOptions(value)
	{
		if (value != "open")
		{
			document.getElementById('trNbQuestions').style.visibility = 'visible';
			document.getElementById('trSuggestion').style.visibility = 'visible';
		}
	}
	
</script>
</HEAD>
<%
if (action.equals("FirstQuestion")) {
      surveyScc.setSessionQuestions(new Vector(10, 2));
      action = "CreateQuestion";
}
if (action.equals("SendNewQuestion")) {
      Question questionObject = new Question(null, null, question, "", "", null, style, 0);
      questionObject.setAnswers(answers);
      Vector questionsV = surveyScc.getSessionQuestions();
      questionsV.add(questionObject);
      action = "CreateQuestion";
} //End if action = ViewResult
else if (action.equals("End")) {
      out.println("<BODY>");
      QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
      //Vector 2 Collection
      Vector questionsV = surveyScc.getSessionQuestions();
      ArrayList q = new ArrayList();
      for (int j = 0; j < questionsV.size(); j++) {
            q.add((Question) questionsV.get(j));
      }
      surveyDetail.setQuestions(q);
      out.println("</BODY></HTML>");
}
if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm"))) {
      out.println("<BODY>");
      Vector questionsV = surveyScc.getSessionQuestions();
      int questionNb = questionsV.size() + 1;
      cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
      buttonPane = gef.getButtonPane();
      if (action.equals("CreateQuestion")) {
            validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            finishButton = (Button) gef.getFormButton(resources.getString("Finish"), "javascript:onClick=goToEnd()", false);
            question = "";
            nbAnswers = "";
            suggestion = "";
            nextAction="SendQuestionForm";
            buttonPane.addButton(validateButton);
            if (questionsV.size() != 0) {
                buttonPane.addButton(finishButton);
            }
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      } else if (action.equals("SendQuestionForm")) {
            validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            if (suggestion.equals("0"))
                suggestionCheck = "";
            else
                suggestionCheck = "checked";
            nextAction="SendNewQuestion";
            buttonPane.addButton(validateButton);
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      }
      
      Window window = gef.getWindow();
      Frame frame = gef.getFrame();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(surveyScc.getSpaceLabel());
      browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
      browseBar.setExtraInformation(resources.getString("SurveyCreation"));

      out.println(window.printBefore());
      out.println(frame.printBefore());
      out.println("<center>");
      displaySurveyHeader(surveyScc.getSessionSurveyUnderConstruction(), resources, surveyScc, out, gef);
      out.println("<BR>");
      Board board = gef.getBoard();
      out.println(board.printBefore());
%>
      <!--DEBUT CORPS -->
      <BR>
      
      <TABLE border="0" cellPadding="3" cellSpacing="0" width="100%" class="intfdcolor4">
        <form name="surveyForm" Action="questionCreator.jsp" method="POST" ENCTYPE="multipart/form-data">
        <tr><td class="txtlibform"><%=resources.getString("SurveyCreationQuestion")%> <%=questionNb%> :</td><td><input type="text" name="question" value="<%=Encode.javaStringToHtmlString(question)%>" size="60" maxlength="<%=DBUtil.TextFieldLength%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td></tr>
        <% if (action.equals("SendQuestionForm")) {
                //if (openQuestion.equals("0")) {
                if (!style.equals("open")) 
                {
                	// question ferm�e
                    out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("survey.style")+" :</td><td>"+resources.getString("survey."+style));
                    //out.println("<tr style=\"visibility: hidden;\"><td class=\"txtlibform\" valign=top>"+resources.getString("survey.style")+" :</td><td>");
                	out.println(" <select style=\"visibility: hidden;\" id=\"questionStyle\" name=\"questionStyle\" value="+style+"><option selected>"+style+"</option></select>");
                	out.println("</td></tr>");
                
                    out.println("<tr><td class=\"txtlibform\">"+resources.getString("SurveyCreationNbPossibleAnswer")+" :</td><td><input type=\"text\" name=\"nbAnswers\" value=\""+nbAnswers+"\" size=\"3\" disabled maxlength=\"2\">&nbsp;<img border=0 src=\""+mandatoryField+"\" width=5 height=5></td></tr>");
                    out.println("<tr><td class=\"txtlibform\">"+resources.getString("SuggestionAllowed")+" :</td><td><input type=\"checkbox\" name=\"suggestion\" value=\"\" "+suggestionCheck+" disabled></td></tr>");
                    //out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("SurveyCreationQuestionTypeMany")+" :</td><td><input type=\"checkbox\" name=\"qcm\" value=\"1\" "+qcmCheck+"></td></tr>");
                    nb = new Integer(nbAnswers).intValue();
                    String inputName = "";
                    int j=0;
                    for (int i = 0; i < nb; i++) 
                    {
                        j = i + 1;
                        inputName = "answer"+i;
                        out.println("<tr><td colspan=2 align=center>");
                        out.println("<table cellpadding=0 cellspacing=5 width=\"100%\">");
                        	out.println("<tr><td class=\"intfdcolor\"><img src=\""+px+"\" border=\"0\"></td></tr>");
                        out.println("</table></td></tr>");
                        out.println("<tr><td class=\"txtlibform\">"+resources.getString("SurveyCreationAnswerNb")+"&nbsp;"+j+" :</td><td><input type=\"text\" name=\""+inputName+"\" value=\"\" size=\"60\" maxlength=\""+DBUtil.TextFieldLength+"\"></td></tr>");

                        if (!style.equals("list"))
                        {
                        	// afficher les photos que si on est pas dans le choix d'une liste d�roulante
                        	out.println("<tr><td class=\"txtlibform\">"+resources.getString("SurveyCreationAnswerImage")+"&nbsp;"+j+" :</td><td><input type=\"file\" name=\"image"+i+"\" size=\"60\"></td></tr>");
                        
	                     	//zone pour le lien vers l'image
	                        out.println("<tr><td></td><td><span id=\"imageGallery"+i+"\"></span>");
	                        out.println("<input type=\"hidden\" id=\"valueImageGallery"+i+"\" name=\"valueImageGallery"+i+"\" >");
	
	                        List galleries = surveyScc.getGalleries();
	                        if (galleries != null)
	    					{
	    						out.println(" <select id=\"galleries\" name=\"galleries\" onchange=\"choixGallery(this, '"+i+"');this.selectedIndex=0;\"> ");
	    						out.println(" <option selected>"+resources.getString("survey.galleries")+"</option> ");
	   							for(int k=0; k < galleries.size(); k++ ) 
	   							{
	   								ComponentInstLight gallery = (ComponentInstLight) galleries.get(k);
	   								out.println(" <option value=\""+gallery.getId()+"\">"+gallery.getLabel()+"</option> ");
	   							}
	    						out.println("</select>");
	    						out.println("</td>");
	    					}
	                    	out.println("</tr>");
                        }
                     	
                    }
                    if (!suggestion.equals("0"))
                    {
                    	out.println("<tr><td colspan=2 align=center><table cellpadding=0 cellspacing=5 width=\"100%\">");
                        out.println("<tr><td class=\"intfdcolor\"><img src=\""+px+"\" border=\"0\"></td></tr>");
                        out.println("</table></td></tr>");
                        out.println("<tr><td class=\"txtlibform\">"+resources.getString("OtherAnswer")+"&nbsp;:</td><td><input type=\"text\" name=\"suggestionLabel\" value=\""+resources.getString("SurveyCreationDefaultSuggestionLabel")+"\" size=\"60\" maxlength=\""+DBUtil.TextFieldLength+"\"></td></tr>");
                    }
                } 
                else 
                {
                	// question ouverte
                	out.println("<input type=\"hidden\" name=\"style\" value="+style+" >");
                	//openQuestionCheck = "checked";
                    //out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("SurveyCreationQuestionOpen")+" :</td><td><input type=\"checkbox\" name=\"openQuestion\" value=\"1\" "+openQuestionCheck+" onClick=\"javaScript:changeQuestionType()\"></td></tr>");
                }
                out.println("<tr><td>(<img border=0 src=\""+mandatoryField+"\" width=5 height=5>&nbsp;:&nbsp;"+generalMessage.getString("GML.requiredField")+")</td></tr>");
           } 
           else 
           {
        	    
                //out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("SurveyCreationQuestionOpen")+" :</td><td><input type=\"checkbox\" name=\"openQuestion\" value=\"1\" "+openQuestionCheck+" onClick=\"javaScript:changeQuestionType()\"></td></tr>");
                // liste d�roulante des choix possible
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("survey.style")+" :</td><td>");
                out.println(" <select id=\"questionStyle\" name=\"questionStyle\" onchange=\"showQuestionOptions(this.value);\"> ");
    			out.println(" <option selected value=\"null\">"+resources.getString("survey.style")+"</option> ");
				out.println(" <option value=\"open\">"+resources.getString("survey.open")+"</option> ");
				out.println(" <option value=\"radio\">"+resources.getString("survey.radio")+"</option> ");
				out.println(" <option value=\"checkbox\">"+resources.getString("survey.checkbox")+"</option> ");
				out.println(" <option value=\"list\">"+resources.getString("survey.list")+"</option> ");
    			out.println("</select>");
                out.println("</td></tr>");
                
                out.println("<tr id=\"trNbQuestions\" style=\"visibility: hidden;\"><td class=\"txtlibform\">"+resources.getString("SurveyCreationNbPossibleAnswer")+" :</td><td><input type=\"text\" name=\"nbAnswers\" value=\""+nbAnswers+"\" size=\"3\"  maxlength=\"2\">&nbsp;<img border=0 src=\""+mandatoryField+"\" width=5 height=5></td></tr>");
                out.println("<tr id=\"trSuggestion\" style=\"visibility: hidden;\"><td class=\"txtlibform\">"+resources.getString("SuggestionAllowed")+" :</td><td><input type=\"checkbox\" name=\"suggestion\" value=\"\" "+suggestionCheck+"></td></tr>");

                String inputName = "answer"+0;
                out.println("<tr><td><input type=\"hidden\" name=\""+inputName+"\"></td></tr>");
                out.println("<tr><td>(<img border=0 src=\""+mandatoryField+"\" width=5 height=5>&nbsp;:&nbsp;"+generalMessage.getString("GML.requiredField")+")</td></tr>");
           }
        %>                                                                             
        <tr><td><input type="hidden" name="Action" value="<%=nextAction%>">
                <input type="hidden" name="SuggestionAllowed" value="0"></td></tr>
      </form>
      </table>
       <!-- FIN CORPS -->
      
<%
	  out.println(board.printAfter());
      out.println(frame.printMiddle());
      out.println("<BR><center>"+buttonPane.print()+"</center>");
      out.println("</center>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
      out.println("</BODY></HTML>");
 } //End if action = ViewQuestion
if (action.equals("End")) {
%>
<HTML>
<HEAD>
<script language="Javascript">
    function goToSurveyPreview() {
        document.questionForm.submit();
    }
</script>
</HEAD>
<BODY onLoad="goToSurveyPreview()">
<Form name="questionForm" Action="surveyDetail.jsp" Method="POST">
<input type="hidden" name="Action" value="PreviewSurvey">
</Form>
</BODY>
</HTML>
<% } %>