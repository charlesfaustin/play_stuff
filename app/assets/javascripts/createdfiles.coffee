$ ->
  $.get "/createdfiles", (crtdfiles) ->
    $.each crtdfiles, (index, crtdfile) ->
      $("#createdfiles").append $("<li>").text crtdfile.idstring
      $("#createdfiles").append $("<a href=/serve/" + crtdfile.idstring + " >").text "download "