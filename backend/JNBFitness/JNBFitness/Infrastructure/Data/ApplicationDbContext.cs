using JNBFitness.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Reflection.Emit;

namespace JNBFitness.Infrastructure.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options)
        {
        }

        // DbSets - Tables principales
        public DbSet<Utilisateur> Utilisateurs { get; set; }
        public DbSet<Client> Clients { get; set; }
        public DbSet<Coach> Coachs { get; set; }
        public DbSet<Administrateur> Administrateurs { get; set; }

        public DbSet<Carte> Cartes { get; set; }
        public DbSet<Transfert> Transferts { get; set; }
        public DbSet<EcritureLedger> EcrituresLedger { get; set; }

        public DbSet<TypeAbonnementConfig> TypeAbonnementConfigs { get; set; }
        public DbSet<AbonnementClient> AbonnementsClients { get; set; }
        public DbSet<Paiement> Paiements { get; set; }

        public DbSet<DisponibiliteCoach> DisponibilitesCoach { get; set; }
        public DbSet<ReservationCoaching> ReservationsCoaching { get; set; }
        public DbSet<NotationCoach> NotationsCoach { get; set; }

        public DbSet<CoursCollectif> CoursCollectifs { get; set; }
        public DbSet<SeanceCoursCollectif> SeancesCoursCollectifs { get; set; }
        public DbSet<ReservationCoursCollectif> ReservationsCoursCollectifs { get; set; }

        public DbSet<Article> Articles { get; set; }
        public DbSet<Notification> Notifications { get; set; }
        public DbSet<Parrainage> Parrainages { get; set; }
        public DbSet<Produit> Produits { get; set; }

        // DbSets - Vues (Keyless)
        public DbSet<VwAbonnementsActifs> VwAbonnementsActifs { get; set; }
        public DbSet<VwParrainagesValides> VwParrainagesValides { get; set; }
        public DbSet<VwSoldesCartes> VwSoldesCartes { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Configuration Utilisateur
            modelBuilder.Entity<Utilisateur>(entity =>
            {
                entity.ToTable("utilisateurs");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Email).HasColumnName("email").IsRequired().HasMaxLength(255);
                entity.Property(e => e.MotDePasse).HasColumnName("mot_de_passe").IsRequired().HasMaxLength(255);
                entity.Property(e => e.Nom).HasColumnName("nom").IsRequired().HasMaxLength(100);
                entity.Property(e => e.Prenom).HasColumnName("prenom").IsRequired().HasMaxLength(100);
                entity.Property(e => e.Telephone).HasColumnName("telephone").HasMaxLength(50);
                entity.Property(e => e.Adresse).HasColumnName("adresse").HasMaxLength(255);
                entity.Property(e => e.Photo).HasColumnName("photo").HasMaxLength(255);
                entity.Property(e => e.TypeUtilisateur).HasColumnName("type_utilisateur").HasConversion<string>();
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();
                entity.Property(e => e.DateInscription).HasColumnName("date_inscription");
                entity.Property(e => e.AbonneNewsletter).HasColumnName("abonne_newsletter");

                entity.HasIndex(e => e.Email).IsUnique();
            });

            // Configuration Client
            modelBuilder.Entity<Client>(entity =>
            {
                entity.ToTable("clients");
                entity.HasKey(e => e.UtilisateurId);
                entity.Property(e => e.UtilisateurId).HasColumnName("utilisateur_id");
                entity.Property(e => e.DateActivation).HasColumnName("date_activation");
                entity.Property(e => e.CodeParrainage).HasColumnName("code_parrainage").HasMaxLength(50);
                entity.Property(e => e.NombreParrainagesValides).HasColumnName("nombre_parrainages_valides");
                entity.Property(e => e.ParrainePar).HasColumnName("parraine_par").HasMaxLength(50);

                entity.HasOne(e => e.Utilisateur)
                    .WithOne()
                    .HasForeignKey<Client>(e => e.UtilisateurId)
                    .OnDelete(DeleteBehavior.Cascade);

                entity.HasIndex(e => e.CodeParrainage).IsUnique();
            });

            // Configuration Coach
            modelBuilder.Entity<Coach>(entity =>
            {
                entity.ToTable("coachs");
                entity.HasKey(e => e.UtilisateurId);
                entity.Property(e => e.UtilisateurId).HasColumnName("utilisateur_id");
                entity.Property(e => e.Specialites).HasColumnName("specialites").HasMaxLength(500);
                entity.Property(e => e.Description).HasColumnName("description").HasColumnType("TEXT");
                entity.Property(e => e.NoteGlobale).HasColumnName("note_globale").HasColumnType("DECIMAL(3,2)");
                entity.Property(e => e.NombreAvis).HasColumnName("nombre_avis");

                entity.HasOne(e => e.Utilisateur)
                    .WithOne()
                    .HasForeignKey<Coach>(e => e.UtilisateurId)
                    .OnDelete(DeleteBehavior.Cascade);
            });

            // Configuration Administrateur
            modelBuilder.Entity<Administrateur>(entity =>
            {
                entity.ToTable("administrateurs");
                entity.HasKey(e => e.UtilisateurId);
                entity.Property(e => e.UtilisateurId).HasColumnName("utilisateur_id");

                entity.HasOne(e => e.Utilisateur)
                    .WithOne()
                    .HasForeignKey<Administrateur>(e => e.UtilisateurId)
                    .OnDelete(DeleteBehavior.Cascade);
            });

            // Configuration Carte
            modelBuilder.Entity<Carte>(entity =>
            {
                entity.ToTable("cartes");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.UtilisateurId).HasColumnName("utilisateur_id");
                entity.Property(e => e.Numero).HasColumnName("numero").IsRequired().HasMaxLength(50);
                entity.Property(e => e.Libelle).HasColumnName("libelle").HasMaxLength(100);
                entity.Property(e => e.Devise).HasColumnName("devise").HasMaxLength(3);
                entity.Property(e => e.SoldeCent).HasColumnName("solde_cent");
                entity.Property(e => e.Active).HasColumnName("active");
                entity.Property(e => e.DateCreation).HasColumnName("date_creation");
                entity.Property(e => e.DateMiseAJour).HasColumnName("date_mise_a_jour");

                entity.HasOne(e => e.Utilisateur)
                    .WithOne(u => u.Carte)
                    .HasForeignKey<Carte>(e => e.UtilisateurId);

                entity.HasIndex(e => e.Numero).IsUnique();
            });

            // Configuration Transfert
            modelBuilder.Entity<Transfert>(entity =>
            {
                entity.ToTable("transferts");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Reference).HasColumnName("reference").HasMaxLength(100);
                entity.Property(e => e.EmetteurCarteId).HasColumnName("emetteur_carte_id");
                entity.Property(e => e.RecepteurCarteId).HasColumnName("recepteur_carte_id");
                entity.Property(e => e.MontantCent).HasColumnName("montant_cent");
                entity.Property(e => e.Devise).HasColumnName("devise").HasMaxLength(3);
                entity.Property(e => e.Motif).HasColumnName("motif").HasMaxLength(255);
                entity.Property(e => e.DateTransfert).HasColumnName("date_transfert");
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();

                entity.HasOne(e => e.CarteEmetteur)
                    .WithMany(c => c.TransfertsEmis)
                    .HasForeignKey(e => e.EmetteurCarteId)
                    .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(e => e.CarteRecepteur)
                    .WithMany(c => c.TransfertsRecus)
                    .HasForeignKey(e => e.RecepteurCarteId)
                    .OnDelete(DeleteBehavior.Restrict);

                entity.HasIndex(e => e.Reference).IsUnique();
            });

            // Configuration EcritureLedger
            modelBuilder.Entity<EcritureLedger>(entity =>
            {
                entity.ToTable("ecritures_ledger");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.TransfertId).HasColumnName("transfert_id");
                entity.Property(e => e.CarteId).HasColumnName("carte_id");
                entity.Property(e => e.Sens).HasColumnName("sens").HasConversion<string>();
                entity.Property(e => e.MontantCent).HasColumnName("montant_cent");
                entity.Property(e => e.DateEcriture).HasColumnName("date_ecriture");

                entity.HasOne(e => e.Transfert)
                    .WithMany(t => t.Ecritures)
                    .HasForeignKey(e => e.TransfertId);

                entity.HasOne(e => e.Carte)
                    .WithMany(c => c.Ecritures)
                    .HasForeignKey(e => e.CarteId);
            });

            // Configuration TypeAbonnementConfig
            modelBuilder.Entity<TypeAbonnementConfig>(entity =>
            {
                entity.ToTable("type_abonnement_config");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Type).HasColumnName("type").HasConversion<string>();
                entity.Property(e => e.Nom).HasColumnName("nom").IsRequired().HasMaxLength(100);
                entity.Property(e => e.Description).HasColumnName("description").HasColumnType("TEXT");
                entity.Property(e => e.DureeEnMois).HasColumnName("duree_en_mois");
                entity.Property(e => e.NombreSeances).HasColumnName("nombre_seances");
                entity.Property(e => e.Prix).HasColumnName("prix").HasColumnType("DECIMAL(10,2)");
                entity.Property(e => e.Actif).HasColumnName("actif");
            });

            // Configuration AbonnementClient
            modelBuilder.Entity<AbonnementClient>(entity =>
            {
                entity.ToTable("abonnements_client");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ClientId).HasColumnName("client_id");
                entity.Property(e => e.TypeAbonnementId).HasColumnName("type_abonnement_id");
                entity.Property(e => e.DateDebut).HasColumnName("date_debut");
                entity.Property(e => e.DateFin).HasColumnName("date_fin");
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();
                entity.Property(e => e.SeancesRestantes).HasColumnName("seances_restantes");
                entity.Property(e => e.OffertParParrainage).HasColumnName("offert_par_parrainage");

                entity.HasOne(e => e.Client)
                    .WithMany(c => c.Abonnements)
                    .HasForeignKey(e => e.ClientId);

                entity.HasOne(e => e.TypeAbonnement)
                    .WithMany(t => t.Abonnements)
                    .HasForeignKey(e => e.TypeAbonnementId);
            });

            // Configuration Paiement
            modelBuilder.Entity<Paiement>(entity =>
            {
                entity.ToTable("paiements");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ClientId).HasColumnName("client_id");
                entity.Property(e => e.AbonnementId).HasColumnName("abonnement_id");
                entity.Property(e => e.TransfertId).HasColumnName("transfert_id");
                entity.Property(e => e.Montant).HasColumnName("montant").HasColumnType("DECIMAL(10,2)");
                entity.Property(e => e.DatePaiement).HasColumnName("date_paiement");
                entity.Property(e => e.MethodePaiement).HasColumnName("methode_paiement").HasMaxLength(50);
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();

                entity.HasOne(e => e.Client)
                    .WithMany(c => c.Paiements)
                    .HasForeignKey(e => e.ClientId);

                entity.HasOne(e => e.Abonnement)
                    .WithMany(a => a.Paiements)
                    .HasForeignKey(e => e.AbonnementId);

                entity.HasOne(e => e.Transfert)
                    .WithOne(t => t.Paiement)
                    .HasForeignKey<Paiement>(e => e.TransfertId);
            });

            // Configuration DisponibiliteCoach
            modelBuilder.Entity<DisponibiliteCoach>(entity =>
            {
                entity.ToTable("disponibilites_coach");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.CoachId).HasColumnName("coach_id");
                entity.Property(e => e.JourSemaine).HasColumnName("jour_semaine").HasMaxLength(20);
                entity.Property(e => e.HeureDebut).HasColumnName("heure_debut");
                entity.Property(e => e.HeureFin).HasColumnName("heure_fin");
                entity.Property(e => e.Actif).HasColumnName("actif");

                entity.HasOne(e => e.Coach)
                    .WithMany(c => c.Disponibilites)
                    .HasForeignKey(e => e.CoachId);
            });

            // Configuration ReservationCoaching
            modelBuilder.Entity<ReservationCoaching>(entity =>
            {
                entity.ToTable("reservations_coaching");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ClientId).HasColumnName("client_id");
                entity.Property(e => e.CoachId).HasColumnName("coach_id");
                entity.Property(e => e.DateSeance).HasColumnName("date_seance");
                entity.Property(e => e.DureeMinutes).HasColumnName("duree_minutes");
                entity.Property(e => e.TypeSeance).HasColumnName("type_seance").HasConversion<string>();
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();
                entity.Property(e => e.DateReservation).HasColumnName("date_reservation");
                entity.Property(e => e.Montant).HasColumnName("montant").HasColumnType("DECIMAL(10,2)");

                entity.HasOne(e => e.Client)
                    .WithMany(c => c.ReservationsCoaching)
                    .HasForeignKey(e => e.ClientId);

                entity.HasOne(e => e.Coach)
                    .WithMany(c => c.ReservationsCoaching)
                    .HasForeignKey(e => e.CoachId);
            });

            // Configuration NotationCoach
            modelBuilder.Entity<NotationCoach>(entity =>
            {
                entity.ToTable("notations_coach");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ClientId).HasColumnName("client_id");
                entity.Property(e => e.CoachId).HasColumnName("coach_id");
                entity.Property(e => e.ReservationCoachingId).HasColumnName("reservation_coaching_id");
                entity.Property(e => e.Note).HasColumnName("note");
                entity.Property(e => e.Commentaire).HasColumnName("commentaire").HasColumnType("TEXT");
                entity.Property(e => e.DateNotation).HasColumnName("date_notation");

                entity.HasOne(e => e.Client)
                    .WithMany(c => c.Notations)
                    .HasForeignKey(e => e.ClientId);

                entity.HasOne(e => e.Coach)
                    .WithMany(c => c.Notations)
                    .HasForeignKey(e => e.CoachId);

                entity.HasOne(e => e.ReservationCoaching)
                    .WithOne(r => r.Notation)
                    .HasForeignKey<NotationCoach>(e => e.ReservationCoachingId);
            });

            // Configuration CoursCollectif
            modelBuilder.Entity<CoursCollectif>(entity =>
            {
                entity.ToTable("cours_collectifs");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Nom).HasColumnName("nom").IsRequired().HasMaxLength(100);
                entity.Property(e => e.Description).HasColumnName("description").HasColumnType("TEXT");
                entity.Property(e => e.CoachId).HasColumnName("coach_id");
                entity.Property(e => e.JourSemaine).HasColumnName("jour_semaine").HasMaxLength(20);
                entity.Property(e => e.HeureDebut).HasColumnName("heure_debut");
                entity.Property(e => e.HeureFin).HasColumnName("heure_fin");
                entity.Property(e => e.CapaciteMax).HasColumnName("capacite_max");
                entity.Property(e => e.Actif).HasColumnName("actif");

                entity.HasOne(e => e.Coach)
                    .WithMany(c => c.CoursCollectifs)
                    .HasForeignKey(e => e.CoachId);
            });

            // Configuration SeanceCoursCollectif
            modelBuilder.Entity<SeanceCoursCollectif>(entity =>
            {
                entity.ToTable("seances_cours_collectifs");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.CoursCollectifId).HasColumnName("cours_collectif_id");
                entity.Property(e => e.DateSeance).HasColumnName("date_seance");
                entity.Property(e => e.PlacesDisponibles).HasColumnName("places_disponibles");
                entity.Property(e => e.Annulee).HasColumnName("annulee");

                entity.HasOne(e => e.CoursCollectif)
                    .WithMany(c => c.Seances)
                    .HasForeignKey(e => e.CoursCollectifId);
            });

            // Configuration ReservationCoursCollectif
            modelBuilder.Entity<ReservationCoursCollectif>(entity =>
            {
                entity.ToTable("reservations_cours_collectifs");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ClientId).HasColumnName("client_id");
                entity.Property(e => e.SeanceCoursCollectifId).HasColumnName("seance_cours_collectif_id");
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();
                entity.Property(e => e.DateReservation).HasColumnName("date_reservation");
                entity.Property(e => e.DelaiAnnulationHeures).HasColumnName("delai_annulation_heures");

                entity.HasOne(e => e.Client)
                    .WithMany(c => c.ReservationsCoursCollectifs)
                    .HasForeignKey(e => e.ClientId);

                entity.HasOne(e => e.SeanceCoursCollectif)
                    .WithMany(s => s.Reservations)
                    .HasForeignKey(e => e.SeanceCoursCollectifId);
            });

            // Configuration Article
            modelBuilder.Entity<Article>(entity =>
            {
                entity.ToTable("articles");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.CoachId).HasColumnName("coach_id");
                entity.Property(e => e.Titre).HasColumnName("titre").IsRequired().HasMaxLength(255);
                entity.Property(e => e.Contenu).HasColumnName("contenu").HasColumnType("MEDIUMTEXT");
                entity.Property(e => e.ImageUrl).HasColumnName("image_url").HasMaxLength(255).IsRequired(false);
                entity.Property(e => e.Statut).HasColumnName("statut").HasConversion<string>();
                entity.Property(e => e.DateCreation).HasColumnName("date_creation");
                entity.Property(e => e.DatePublication).HasColumnName("date_publication");
                entity.Property(e => e.DateValidation).HasColumnName("date_validation");
                entity.Property(e => e.CommentaireAdmin).HasColumnName("commentaire_admin").HasMaxLength(255).IsRequired(false);

                entity.HasOne(e => e.Coach)
                    .WithMany(c => c.Articles)
                    .HasForeignKey(e => e.CoachId);
            });

            // Configuration Notification
            modelBuilder.Entity<Notification>(entity =>
            {
                entity.ToTable("notifications");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.DestinataireId).HasColumnName("destinataire_id");
                entity.Property(e => e.Titre).HasColumnName("titre").HasMaxLength(255);
                entity.Property(e => e.Message).HasColumnName("message").HasColumnType("TEXT");
                entity.Property(e => e.Type).HasColumnName("type").HasMaxLength(50);
                entity.Property(e => e.DateEnvoi).HasColumnName("date_envoi");
                entity.Property(e => e.Lue).HasColumnName("lue");
                entity.Property(e => e.EmailEnvoye).HasColumnName("email_envoye");

                entity.HasOne(e => e.Destinataire)
                    .WithMany(u => u.Notifications)
                    .HasForeignKey(e => e.DestinataireId);
            });

            // Configuration Parrainage
            modelBuilder.Entity<Parrainage>(entity =>
            {
                entity.ToTable("parrainages");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ParrainId).HasColumnName("parrain_id");
                entity.Property(e => e.FilleulId).HasColumnName("filleul_id");
                entity.Property(e => e.DateInscriptionFilleul).HasColumnName("date_inscription_filleul");
                entity.Property(e => e.Valide).HasColumnName("valide");
                entity.Property(e => e.DateValidation).HasColumnName("date_validation");
                entity.Property(e => e.MoisGratuitAttribue).HasColumnName("mois_gratuit_attribue");

                entity.HasOne(e => e.Parrain)
                    .WithMany(c => c.ParrainagesEffectues)
                    .HasForeignKey(e => e.ParrainId)
                    .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(e => e.Filleul)
                    .WithMany(c => c.ParrainagesRecus)
                    .HasForeignKey(e => e.FilleulId)
                    .OnDelete(DeleteBehavior.Restrict);
            });

            // Configuration Produit
            modelBuilder.Entity<Produit>(entity =>
            {
                entity.ToTable("produits");
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Nom).HasColumnName("nom").IsRequired().HasMaxLength(100);
                entity.Property(e => e.Description).HasColumnName("description").HasColumnType("TEXT");
                entity.Property(e => e.Prix).HasColumnName("prix").HasColumnType("DECIMAL(10,2)");
                entity.Property(e => e.Categorie).HasColumnName("categorie").HasMaxLength(50);
                entity.Property(e => e.ImageUrl).HasColumnName("image_url").HasMaxLength(255).IsRequired(false);
                entity.Property(e => e.Actif).HasColumnName("actif");
            });

            // Configuration des Vues (Keyless Entities)
            modelBuilder.Entity<VwAbonnementsActifs>(entity =>
            {
                entity.ToView("vw_abonnements_actifs");
                entity.HasNoKey();
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ClientId).HasColumnName("client_id");
                entity.Property(e => e.TypeAbonnementId).HasColumnName("type_abonnement_id");
                entity.Property(e => e.TypeNom).HasColumnName("type_nom");
                entity.Property(e => e.DateDebut).HasColumnName("date_debut");
                entity.Property(e => e.DateFin).HasColumnName("date_fin");
            });

            modelBuilder.Entity<VwParrainagesValides>(entity =>
            {
                entity.ToView("vw_parrainages_valides");
                entity.HasNoKey();
                entity.Property(e => e.ParrainId).HasColumnName("parrain_id");
                entity.Property(e => e.NbFilleulsValides).HasColumnName("nb_filleuls_valides");
            });

            modelBuilder.Entity<VwSoldesCartes>(entity =>
            {
                entity.ToView("vw_soldes_cartes");
                entity.HasNoKey();
                entity.Property(e => e.CarteId).HasColumnName("carte_id");
                entity.Property(e => e.UtilisateurId).HasColumnName("utilisateur_id");
                entity.Property(e => e.Numero).HasColumnName("numero");
                entity.Property(e => e.SoldeCent).HasColumnName("solde_cent");
                entity.Property(e => e.TotalCredits).HasColumnName("total_credits");
                entity.Property(e => e.TotalDebits).HasColumnName("total_debits");
                entity.Property(e => e.Ecart).HasColumnName("ecart");
            });
        }
    }
}
