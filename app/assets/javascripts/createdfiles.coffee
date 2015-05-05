$ ->
  $.get "/createdfiles", (crtdfiles) ->
    $.each crtdfiles, (index, crtdfile) ->
      $("#createdfiles").append $("<li>").text crtdfile.idstring
      $("#createdfiles").append $("<b>").append $("<a href=/serve/" + crtdfile.idstring + " >").text "download "