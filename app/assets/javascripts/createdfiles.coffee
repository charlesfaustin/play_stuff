$ ->
  $.get "/createdfiles", (crtdfiles) ->
    $.each crtdfiles, (index, crtdfile) ->
      $("#createdfiles").append $("<li>").text crtdfile.filename 
      $("#createdfiles").append $("<b>").text "download "