$ ->
  $.get "/musics", (musics) ->
    $.each musics, (index, music) ->
      $("#musicid").append $("<li>").text music.filename   # + "  " + music.idstring 