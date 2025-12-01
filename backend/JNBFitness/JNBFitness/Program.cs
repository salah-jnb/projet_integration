using JNBFitness.Application.Services.Abonnement;
using JNBFitness.Application.Services.Article;
using JNBFitness.Application.Services.Auth;
using JNBFitness.Application.Services.Cours;
using JNBFitness.Application.Services.Notification;
using JNBFitness.Application.Services.Paiement;
using JNBFitness.Application.Services.Parrainage;
using JNBFitness.Application.Services.Produit;
using JNBFitness.Application.Services.Reservation;
using JNBFitness.Application.Services.Utilisateur;
using JNBFitness.Application.Services.Notation;
using JNBFitness.Infrastructure.Data;
using JNBFitness.Infrastructure.Repositories;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// ==================== CONFIGURATION BASE DE DONNÉES ====================
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString))
           .LogTo(Console.WriteLine, LogLevel.Information)
           .EnableSensitiveDataLogging()
           .EnableDetailedErrors());

// ==================== CONFIGURATION AUTOMAPPER ====================
builder.Services.AddAutoMapper(AppDomain.CurrentDomain.GetAssemblies());

// ==================== INJECTION DES DÉPENDANCES - REPOSITORIES ====================
builder.Services.AddScoped(typeof(IRepository<>), typeof(Repository<>));
builder.Services.AddScoped<IUtilisateurRepository, UtilisateurRepository>();
builder.Services.AddScoped<IClientRepository, ClientRepository>();
builder.Services.AddScoped<ICoachRepository, CoachRepository>();
builder.Services.AddScoped<IAbonnementRepository, AbonnementRepository>();
builder.Services.AddScoped<ITypeAbonnementConfigRepository, TypeAbonnementConfigRepository>();
builder.Services.AddScoped<IReservationCoachingRepository, ReservationCoachingRepository>();
builder.Services.AddScoped<IReservationCoursCollectifRepository, ReservationCoursCollectifRepository>();
builder.Services.AddScoped<ICoursCollectifRepository, CoursCollectifRepository>();
builder.Services.AddScoped<ISeanceCoursCollectifRepository, SeanceCoursCollectifRepository>();
builder.Services.AddScoped<ISeanceCoursCollectifRepository, SeanceCoursCollectifRepository>();
builder.Services.AddScoped<IPaiementRepository, PaiementRepository>();
builder.Services.AddScoped<ICarteRepository, CarteRepository>();
builder.Services.AddScoped<ITransfertRepository, TransfertRepository>();
builder.Services.AddScoped<IEcritureLedgerRepository, EcritureLedgerRepository>();
builder.Services.AddScoped<IArticleRepository, ArticleRepository>();
builder.Services.AddScoped<IDisponibiliteCoachRepository, DisponibiliteCoachRepository>();
builder.Services.AddScoped<INotationCoachRepository, NotationCoachRepository>();
builder.Services.AddScoped<INotificationRepository, NotificationRepository>();
builder.Services.AddScoped<IParrainageRepository, ParrainageRepository>();
builder.Services.AddScoped<IProduitRepository, ProduitRepository>();

// ==================== INJECTION DES DÉPENDANCES - SERVICES ====================
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IUtilisateurService, UtilisateurService>();
builder.Services.AddScoped<IClientService, ClientService>();
builder.Services.AddScoped<ICoachService, CoachService>();
builder.Services.AddScoped<IAbonnementService, AbonnementService>();
builder.Services.AddScoped<ITypeAbonnementConfigService, TypeAbonnementConfigService>();
builder.Services.AddScoped<IReservationCoachingService, ReservationCoachingService>();
builder.Services.AddScoped<IReservationCoursService, ReservationCoursService>();
builder.Services.AddScoped<ICoursCollectifService, CoursCollectifService>();
builder.Services.AddScoped<ISeanceCoursCollectifService, SeanceCoursCollectifService>();
builder.Services.AddScoped<IPaiementService, PaiementService>();
builder.Services.AddScoped<ICarteService, CarteService>();
builder.Services.AddScoped<ITransfertService, TransfertService>();
builder.Services.AddScoped<IArticleService, ArticleService>();
builder.Services.AddScoped<INotificationService, NotificationService>();
builder.Services.AddScoped<IParrainageService, ParrainageService>();
builder.Services.AddScoped<IProduitService, ProduitService>();
builder.Services.AddScoped<IDisponibiliteService, DisponibiliteService>();
builder.Services.AddScoped<INotationCoachService, NotationCoachService>();
builder.Services.AddScoped<JNBFitness.Application.Services.Communication.IEmailService, JNBFitness.Application.Services.Communication.EmailService>();
builder.Services.AddHostedService<NotificationQueueHostedService>();

// ==================== CONFIGURATION JWT AUTHENTICATION ====================
var jwtKey = builder.Configuration["Jwt:Key"];
var jwtIssuer = builder.Configuration["Jwt:Issuer"];
var jwtAudience = builder.Configuration["Jwt:Audience"];

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = jwtIssuer,
        ValidAudience = jwtAudience,
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey ?? "default_secret_key_12345678901234567890")),
        // Map custom claim types used when issuing the token
        NameClaimType = System.IdentityModel.Tokens.Jwt.JwtRegisteredClaimNames.Sub,
        RoleClaimType = "role"
    };
});

builder.Services.AddAuthorization();

// ==================== CONFIGURATION DES CONTROLLERS ====================
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
        options.JsonSerializerOptions.DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
    });

// Désactiver la réponse 400 automatique pour ModelState invalide afin de permettre
// un traitement personnalisé du multipart/form-data (ex: parsing décimal invariant)
builder.Services.Configure<Microsoft.AspNetCore.Mvc.ApiBehaviorOptions>(options =>
{
    options.SuppressModelStateInvalidFilter = true;
});

// ==================== CONFIGURATION SWAGGER ====================
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "JNB Fitness API",
        Version = "v1.0",
        Description = @"
# API complète pour la gestion de la salle de sport JNB Fitness

## Fonctionnalités principales :
- 🔐 **Authentification** : Inscription, Connexion, JWT
- 👥 **Utilisateurs** : Clients, Coachs, Administrateurs
- 💳 **Abonnements** : Salle, Cours collectifs, Packs coaching
- 📅 **Réservations** : Coaching privé, Cours collectifs
- 💰 **Paiements** : Cartes virtuelles, Transferts, Ledger
- 📝 **Blog** : Articles rédigés par les coachs
- 🎁 **Parrainage** : Système de parrainage avec récompenses
- 🛒 **Produits** : Catalogue de produits fitness

## Authentification
Utilisez le endpoint `/api/Auth/login` pour obtenir un token JWT.
Cliquez sur 'Authorize' et entrez : `Bearer {votre_token}`",
        Contact = new OpenApiContact
        {
            Name = "JNB Fitness Support",
            Email = "support@jnbfitness.com",
            Url = new Uri("https://jnbfitness.com")
        },
        License = new OpenApiLicense
        {
            Name = "Propriétaire : JNB Fitness",
        }
    });

    // Configuration JWT dans Swagger
    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = @"Authentification JWT via header Authorization.
Entrez 'Bearer' [espace] puis votre token.
Exemple: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    });

    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            new List<string>()
        }
    });

    // Ordre des endpoints par tags
    options.TagActionsBy(api => new[] { api.GroupName ?? api.ActionDescriptor.RouteValues["controller"] });
    options.DocInclusionPredicate((name, api) => true);
});

// ==================== CONFIGURATION CORS ====================
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// ==================== CONFIGURATION DU PIPELINE HTTP ====================

// ⚠️ IMPORTANT : CORS doit être AVANT UseHttpsRedirection
app.UseCors("AllowAll");

app.UseSwagger();
app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/swagger/v1/swagger.json", "JNB Fitness API v1.0");
    c.RoutePrefix = "swagger";
    c.DocumentTitle = "JNB Fitness API - Documentation";
    c.DefaultModelsExpandDepth(2);
    c.DefaultModelExpandDepth(2);
    c.DisplayRequestDuration();
    c.EnableDeepLinking();
    c.EnableFilter();
});

// Redirection vers Swagger par défaut
app.MapGet("/", () => Results.Redirect("/swagger")).ExcludeFromDescription();

if (app.Environment.IsDevelopment())
{
    app.UseDeveloperExceptionPage();
}

// ⚠️ Commenté pour éviter les problèmes CORS en développement
// app.UseHttpsRedirection();

// Servir les fichiers statiques (ex: photos profil, uploads)
app.UseStaticFiles();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// ==================== MESSAGE DE DÉMARRAGE ====================
app.Lifetime.ApplicationStarted.Register(() =>
{
    var addresses = app.Urls;
    Console.WriteLine("\n╔═══════════════════════════════════════════════════════════════╗");
    Console.WriteLine("║         🏋️  JNB FITNESS API - DÉMARRAGE RÉUSSI  🏋️           ║");
    Console.WriteLine("╠═══════════════════════════════════════════════════════════════╣");
    Console.WriteLine($"║  📍 API disponible sur : {string.Join(", ", addresses).PadRight(36)} ║");
    var swaggerUrl = addresses.FirstOrDefault() != null ? addresses.First() + "/swagger" : "http://localhost:5000/swagger";
    Console.WriteLine($"║  📖 Documentation Swagger : {swaggerUrl.PadRight(29)} ║");
    Console.WriteLine($"║  🔐 Base de données : MySQL - jnb_fitness{new string(' ', 19)}║");
    Console.WriteLine($"║  ⏰ Démarré à : {DateTime.Now:dd/MM/yyyy HH:mm:ss}{new string(' ', 34)}║");
    Console.WriteLine("╠═══════════════════════════════════════════════════════════════╣");
    Console.WriteLine("║  📌 Endpoints principaux :                                    ║");
    Console.WriteLine("║     • POST /api/Auth/register - Inscription                   ║");
    Console.WriteLine("║     • POST /api/Auth/login - Connexion                        ║");
    Console.WriteLine("║     • GET  /api/Coachs - Liste des coachs                     ║");
    Console.WriteLine("║     • GET  /api/CoursCollectifs - Cours disponibles           ║");
    Console.WriteLine("║     • GET  /api/Articles - Blog                               ║");
    Console.WriteLine("╚═══════════════════════════════════════════════════════════════╝\n");
});

app.Run();

public partial class Program { }
