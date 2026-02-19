
Este diretório contém a entrada kustomize para o recurso `os-service`.

As configurações foram migradas de `tech_challenge_k8s_infra/microservices/os-service` para o diretório local `k8s/os-service`.

Para aplicar localmente:

```bash
# a partir da raiz do repositório
kustomize build k8s | kubectl apply -f -
```

O `k8s/deployment.yml` referencia agora `./os-service` e contém um `kustomization` que monta os recursos locais.

