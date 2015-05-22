$ ->
  $.get "/createdfiles", (crtdfiles) ->
    $.each crtdfiles, (index, crtdfile) ->
      #$("#advanced-3").append $("<li>").text crtdfile.idstring.substring(0,4)

     
      $("#advanced-3").append $("<li>").append $("<b>").append $("<a href=/serve/" + crtdfile.idstring + " >").text crtdfile.idstring.substring(0,4)