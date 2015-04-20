$ ->
  $.get "/persons", (persons) ->
    $.each persons, (index, person) ->
      $("#persons").append $("<li>").text person.name + ", " + person.age + " " + person.email + " " + person.eligible