package com.proyecto360.health.data

object EveningExamPrompts {
    data class PromptChip(val id: String, val label: String, val hint: String)

    val didWell = listOf(
        PromptChip(
            id = "justas",
            label = "Acciones justas",
            hint = "Ayudaste a alguien o actuaste con honestidad sin buscar aplausos."
        ),
        PromptChip(
            id = "dominio",
            label = "Dominio propio",
            hint = "Mostraste templanza ante una provocación o un contratiempo menor."
        ),
        PromptChip(
            id = "enfoque",
            label = "Bajo tu Control",
            hint = "Invertiste tu energía solo en lo que depende de ti."
        ),
        PromptChip(
            id = "exercise",
            label = "Ejercicio",
            hint = "Moviste el cuerpo: caminaste, entrenaste o cuidaste tu salud física."
        ),
        PromptChip(
            id = "meditation",
            label = "Meditación",
            hint = "Dedicaste tiempo a la quietud, la respiración o la atención plena."
        ),
        PromptChip(
            id = "lectura",
            label = "Lectura",
            hint = "Leíste con atención para aprender o cultivar el carácter."
        ),
        PromptChip(
            id = "educacion",
            label = "Educación",
            hint = "Estudiaste, enseñaste o profundizaste en algo que te forma."
        ),
        PromptChip(
            id = "empatia",
            label = "Empatía",
            hint = "Escuchaste con presencia o te pusiste en el lugar del otro."
        ),
        PromptChip(
            id = "silencio",
            label = "Silencio",
            hint = "Elegiste callar, no reaccionar de inmediato o proteger un espacio de quietud."
        )
    )

    val didNotWell = listOf(
        PromptChip(
            id = "juicios",
            label = "Juicios erróneos",
            hint = "Te dejaste llevar por la ira, la queja o la ansiedad por cosas que no controlas."
        ),
        PromptChip(
            id = "inaccion",
            label = "Falta de acción",
            hint = "Pospusiste una tarea importante o cediste a la comodidad en lugar del deber."
        ),
        PromptChip(
            id = "apego",
            label = "Exceso de apego",
            hint = "Reaccionaste mal ante la opinión de los demás o eventos externos."
        ),
        PromptChip(
            id = "ira",
            label = "Ira",
            hint = "Te dejaste llevar por el enojo o respondiste con dureza."
        ),
        PromptChip(
            id = "chismes",
            label = "Chismes",
            hint = "Hablaste de otros a sus espaldas o alimentaste conversaciones vanas."
        ),
        PromptChip(
            id = "pereza",
            label = "Pereza",
            hint = "Evitaste el esfuerzo debido y cediste a la inercia o la distracción."
        )
    )

    val courseCorrection = listOf(
        PromptChip(
            id = "sin_culpa",
            label = "Sin culpa",
            hint = "No uses el error para castigarte, sino como datos para aprender."
        ),
        PromptChip(
            id = "ajuste",
            label = "Ajuste diario",
            hint = "Define una sola actitud o hábito que cambiarás mañana por la mañana."
        )
    )
}
