package ma.projet.grpc.controllers;

import io.grpc.stub.StreamObserver;
import ma.projet.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final Map<String, Compte> compteDB = new ConcurrentHashMap<>();

    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        GetAllComptesResponse.Builder responseBuilder = GetAllComptesResponse.newBuilder();
        responseBuilder.addAllComptes(compteDB.values());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        Compte compte = compteDB.get(request.getId());
        if (compte != null) {
            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(compte).build());
        } else {
            responseObserver.onError(new Throwable("Compte non trouvé"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        int count = compteDB.size();
        float sum = 0;
        for (Compte compte : compteDB.values()) {
            sum += compte.getSolde();
        }
        float average = count > 0 ? sum / count : 0;

        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(average)
                .build();

        responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
        responseObserver.onCompleted();
    }

    @Override
    public void saveCompte(SaveCompteRequest request, StreamObserver<SaveCompteResponse> responseObserver) {
        CompteRequest compteReq = request.getCompte();
        String id = UUID.randomUUID().toString();

        Compte compte = Compte.newBuilder()
                .setId(id)
                .setSolde(compteReq.getSolde())
                .setDateCreation(compteReq.getDateCreation())
                .setType(compteReq.getType())
                .build();

        compteDB.put(id, compte);

        responseObserver.onNext(SaveCompteResponse.newBuilder().setCompte(compte).build());
        responseObserver.onCompleted();
    }

    @Override
    public void findByType(FindByTypeRequest request, StreamObserver<FindByTypeResponse> responseObserver) {
        List<Compte> comptes = new ArrayList<>();
        for (Compte compte : compteDB.values()) {
            if (compte.getType() == request.getType()) {
                comptes.add(compte);
            }
        }
        responseObserver.onNext(FindByTypeResponse.newBuilder().addAllComptes(comptes).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCompte(DeleteCompteRequest request, StreamObserver<DeleteCompteResponse> responseObserver) {
        Compte removed = compteDB.remove(request.getId());
        if (removed != null) {
            responseObserver.onNext(DeleteCompteResponse.newBuilder().setSuccess(true).build());
        } else {
            responseObserver.onNext(DeleteCompteResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }
}