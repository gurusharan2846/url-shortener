{{- define "analytics.fullname" -}}
{{- .Release.Name -}}
{{- end -}}

{{- define "analytics.namespace" -}}
{{- .Values.namespace -}}
{{- end -}}