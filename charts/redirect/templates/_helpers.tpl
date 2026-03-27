{{- define "redirect.fullname" -}}
{{- .Release.Name -}}
{{- end -}}

{{- define "redirect.namespace" -}}
{{- .Values.namespace -}}
{{- end -}}