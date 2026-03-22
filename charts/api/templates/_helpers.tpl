{{- define "api.fullname" -}}
{{- .Release.Name -}}
{{- end -}}

{{- define "api.namespace" -}}
{{- .Values.namespace -}}
{{- end -}}