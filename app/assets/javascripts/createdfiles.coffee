$ ->
  $.get "/createdfiles", (crtdfiles) ->
    $.each crtdfiles, (index, crtdfile) ->
      $("#musicid").append $("<li>").text crtdfile.filename  