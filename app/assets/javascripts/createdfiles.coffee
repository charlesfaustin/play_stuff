$ ->
  $.get "/createdfiles", (crtdfiles) ->
    $.each crtdfiles, (index, crtdfile) ->
      $("#advanced-2").append $("<li>").text crtdfile.idstring
      #$("#createdfiles").append $("<b>").append $("<a href=/serve/" + crtdfile.idstring + " >").text "download "