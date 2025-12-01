using AutoMapper;
using JNBFitness.Application.DTOs.Abonnement;
using JNBFitness.Application.DTOs.Article;
using JNBFitness.Application.DTOs.Cours;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Application.DTOs.Parrainage;
using JNBFitness.Application.DTOs.Produit;
using JNBFitness.Application.DTOs.Reservation;
using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Application.DTOs.Notation;
using JNBFitness.Domain.Entities;

namespace JNBFitness.Application.Mapping
{
    public class MappingProfile : Profile
    {
        public MappingProfile()
        {
            // ==================== UTILISATEURS ====================

            CreateMap<Utilisateur, UtilisateurDto>()
                .ForMember(dest => dest.TypeUtilisateur, opt => opt.MapFrom(src => src.TypeUtilisateur.ToString()))
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()));

            CreateMap<Client, ClientDto>()
                .ForMember(dest => dest.Email, opt => opt.MapFrom(src => src.Utilisateur.Email))
                .ForMember(dest => dest.Nom, opt => opt.MapFrom(src => src.Utilisateur.Nom))
                .ForMember(dest => dest.Prenom, opt => opt.MapFrom(src => src.Utilisateur.Prenom))
                .ForMember(dest => dest.Telephone, opt => opt.MapFrom(src => src.Utilisateur.Telephone));

            CreateMap<Coach, CoachDto>()
                .ForMember(dest => dest.Email, opt => opt.MapFrom(src => src.Utilisateur.Email))
                .ForMember(dest => dest.Nom, opt => opt.MapFrom(src => src.Utilisateur.Nom))
                .ForMember(dest => dest.Prenom, opt => opt.MapFrom(src => src.Utilisateur.Prenom))
                .ForMember(dest => dest.Photo, opt => opt.MapFrom(src => src.Utilisateur.Photo));

            CreateMap<Coach, CoachDetailsDto>()
                .ForMember(dest => dest.Email, opt => opt.MapFrom(src => src.Utilisateur.Email))
                .ForMember(dest => dest.Nom, opt => opt.MapFrom(src => src.Utilisateur.Nom))
                .ForMember(dest => dest.Prenom, opt => opt.MapFrom(src => src.Utilisateur.Prenom))
                .ForMember(dest => dest.Photo, opt => opt.MapFrom(src => src.Utilisateur.Photo));

            CreateMap<DisponibiliteCoach, DisponibiliteCoachDto>();

            // ==================== ABONNEMENTS ====================

            CreateMap<TypeAbonnementConfig, TypeAbonnementDto>()
                .ForMember(dest => dest.Type, opt => opt.MapFrom(src => src.Type.ToString()));

            CreateMap<AbonnementClient, AbonnementDto>()
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()))
                .ForMember(dest => dest.TypeNom, opt => opt.MapFrom(src => src.TypeAbonnement.Nom));

            CreateMap<AbonnementClient, AbonnementDetailsDto>()
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()))
                .ForMember(dest => dest.ClientNom, opt => opt.MapFrom(src => src.Client.Utilisateur.Nom))
                .ForMember(dest => dest.ClientPrenom, opt => opt.MapFrom(src => src.Client.Utilisateur.Prenom));

            // ==================== RESERVATIONS ====================

            CreateMap<ReservationCoaching, ReservationCoachingDto>()
                .ForMember(dest => dest.TypeSeance, opt => opt.MapFrom(src => src.TypeSeance.ToString()))
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()))
                .ForMember(dest => dest.CoachNom, opt => opt.MapFrom(src => src.Coach.Utilisateur.Nom))
                .ForMember(dest => dest.CoachPrenom, opt => opt.MapFrom(src => src.Coach.Utilisateur.Prenom));

            CreateMap<ReservationCoursCollectif, ReservationCoursCollectifDto>()
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()))
                .ForMember(dest => dest.CoursNom, opt => opt.MapFrom(src => src.SeanceCoursCollectif.CoursCollectif.Nom))
                .ForMember(dest => dest.DateSeance, opt => opt.MapFrom(src => src.SeanceCoursCollectif.DateSeance));

            // ==================== COURS COLLECTIFS ====================

            CreateMap<CoursCollectif, CoursCollectifDto>()
                .ForMember(dest => dest.CoachNom, opt => opt.MapFrom(src => src.Coach.Utilisateur.Nom))
                .ForMember(dest => dest.CoachPrenom, opt => opt.MapFrom(src => src.Coach.Utilisateur.Prenom));

            CreateMap<CreateCoursCollectifDto, CoursCollectif>();

            CreateMap<SeanceCoursCollectif, SeanceCoursCollectifDto>()
                .ForMember(dest => dest.CoursNom, opt => opt.MapFrom(src => src.CoursCollectif.Nom));

            // ==================== PAIEMENTS & CARTES ====================

            CreateMap<Paiement, PaiementDto>()
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()))
                .ForMember(dest => dest.AbonnementNom, opt => opt.MapFrom(src => src.Abonnement.TypeAbonnement.Nom));

            CreateMap<Carte, CarteDto>();

            CreateMap<Transfert, TransfertDto>()
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()));

            // ==================== ARTICLES ====================

            CreateMap<Article, ArticleDto>()
                .ForMember(dest => dest.Statut, opt => opt.MapFrom(src => src.Statut.ToString()))
                .ForMember(dest => dest.CoachNom, opt => opt.MapFrom(src => src.Coach.Utilisateur.Nom))
                .ForMember(dest => dest.CoachPrenom, opt => opt.MapFrom(src => src.Coach.Utilisateur.Prenom));

            // ==================== NOTIFICATIONS ====================

            CreateMap<Notification, NotificationDto>();

            // ==================== NOTATIONS ====================
            CreateMap<NotationCoach, NotationCoachDto>()
                .ForMember(dest => dest.ClientNom, opt => opt.MapFrom(src => src.Client.Utilisateur.Nom))
                .ForMember(dest => dest.ClientPrenom, opt => opt.MapFrom(src => src.Client.Utilisateur.Prenom));

            // ==================== PARRAINAGES ====================

            CreateMap<Parrainage, ParrainageDto>()
                .ForMember(dest => dest.FilleulNom, opt => opt.MapFrom(src => src.Filleul.Utilisateur.Nom))
                .ForMember(dest => dest.FilleulPrenom, opt => opt.MapFrom(src => src.Filleul.Utilisateur.Prenom))
                .ForMember(dest => dest.FilleulEmail, opt => opt.MapFrom(src => src.Filleul.Utilisateur.Email));

            // ==================== PRODUITS ====================

            CreateMap<Produit, ProduitDto>();
            CreateMap<CreateProduitDto, Produit>();
        }
    }
}
