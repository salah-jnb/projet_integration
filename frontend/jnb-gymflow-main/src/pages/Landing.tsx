import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dumbbell, Users, Award, Calendar, Star } from "lucide-react";
import { Link } from "react-router-dom";
import heroImage from "@/assets/hero-dark-1.jpg";
import logo from "@/assets/jnb-logo.png";
import { CoachesSection } from "@/components/CoachesSection";

const Landing = () => {
  const services = [
    {
      icon: Dumbbell,
      title: "Salle de Musculation",
      description: "Équipements modernes et complets pour tous vos entraînements"
    },
    {
      icon: Users,
      title: "Cours Collectifs",
      description: "Yoga, Zumba, Crossfit et plus encore avec nos coachs experts"
    },
    {
      icon: Award,
      title: "Coaching Personnalisé",
      description: "Programmes sur mesure adaptés à vos objectifs"
    },
    {
      icon: Calendar,
      title: "Réservation Flexible",
      description: "Réservez vos séances en ligne 24/7"
    }
  ];

  const testimonials = [
    {
      name: "Pierre L.",
      text: "Excellente salle avec du matériel de qualité. Les coachs sont très professionnels !",
      rating: 5
    },
    {
      name: "Marie D.",
      text: "J'adore les cours collectifs, ambiance au top et résultats visibles rapidement.",
      rating: 5
    }
  ];

  return (
    <div className="min-h-screen">
      {/* Header */}
      <header className="fixed top-0 w-full bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 z-50 border-b animate-fade-in">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <img src={logo} alt="JNB Fitness" className="h-10 w-auto object-contain" />
            <span className="text-2xl font-bold text-accent">JNB FITNESS</span>
          </div>
          <nav className="hidden md:flex items-center gap-6">
            <a href="#services" className="text-foreground hover:text-accent transition-colors">Services</a>
            <a href="#coaches" className="text-foreground hover:text-accent transition-colors">Coachs</a>
            <a href="#testimonials" className="text-foreground hover:text-accent transition-colors">Témoignages</a>
          </nav>
          <div className="flex items-center gap-3">
            <Link to="/login">
              <Button variant="ghost">Connexion</Button>
            </Link>
            <Link to="/register">
              <Button variant="hero">S'inscrire</Button>
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center overflow-hidden pt-16">
        <div 
          className="absolute inset-0 bg-cover bg-no-repeat"
          style={{ 
            backgroundImage: `url(${heroImage})`,
            backgroundPosition: 'center 35%'
          }}
        >
          <div className="absolute inset-0 bg-gradient-to-r from-background/95 via-background/80 to-transparent" />
        </div>
        
        <div className="container mx-auto px-4 relative z-10 text-center animate-fade-in">
          <h1 className="text-5xl md:text-7xl font-bold mb-6 text-foreground animate-slide-up">
            Transformez Votre Corps,
            <br />
            <span className="text-accent animate-scale-in">Transformez Votre Vie</span>
          </h1>
          <p className="text-xl md:text-2xl text-muted-foreground mb-8 max-w-2xl mx-auto animate-fade-in" style={{ animationDelay: '0.2s' }}>
            Rejoignez la meilleure salle de sport avec des équipements modernes, 
            des coachs experts et une communauté motivée.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center animate-fade-in" style={{ animationDelay: '0.4s' }}>
            <Link to="/register">
              <Button size="lg" className="w-full sm:w-auto bg-accent hover:bg-accent-glow text-accent-foreground font-bold px-8 py-6 text-lg transition-all hover:scale-105 hover:shadow-glow">
                Commencer Maintenant
              </Button>
            </Link>
            <Link to="/login">
              <Button variant="outline" size="lg" className="w-full sm:w-auto border-2 border-accent text-accent hover:bg-accent hover:text-accent-foreground font-bold px-8 py-6 text-lg transition-all hover:scale-105">
                Se Connecter
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section id="services" className="py-20 bg-secondary/30">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12 animate-slide-up">
            <h2 className="text-4xl font-bold mb-4">Nos Services</h2>
            <p className="text-muted-foreground text-lg">
              Tout ce dont vous avez besoin pour atteindre vos objectifs
            </p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {services.map((service, index) => (
              <Card key={index} className="group hover:shadow-xl transition-all duration-300 hover:-translate-y-1 animate-scale-in border-2 hover:border-accent">
                <CardContent className="p-6 text-center">
                  <div className="mb-4 inline-flex p-4 rounded-full bg-accent/10 group-hover:bg-accent/20 transition-colors">
                    <service.icon className="h-8 w-8 text-accent" />
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{service.title}</h3>
                  <p className="text-muted-foreground">{service.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Coaches Section */}
      <section id="coaches">
        <CoachesSection />
      </section>

      {/* Testimonials Section */}
      <section id="testimonials" className="py-20 bg-secondary/30">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold mb-4">Témoignages</h2>
            <p className="text-muted-foreground text-lg">
              Ce que nos membres disent de nous
            </p>
          </div>
          
          <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
            {testimonials.map((testimonial, index) => (
              <Card key={index} className="hover:shadow-xl transition-shadow">
                <CardContent className="p-6">
                  <div className="flex gap-1 mb-3">
                    {[...Array(testimonial.rating)].map((_, i) => (
                      <Star key={i} className="h-5 w-5 text-accent fill-accent" />
                    ))}
                  </div>
                  <p className="text-foreground mb-4 italic">"{testimonial.text}"</p>
                  <p className="font-semibold text-primary">- {testimonial.name}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-background via-card to-background relative overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-accent/10 via-transparent to-transparent" />
        <div className="container mx-auto px-4 text-center relative z-10">
          <h2 className="text-4xl font-bold mb-6 animate-fade-in">Prêt à Commencer Votre Transformation ?</h2>
          <p className="text-xl mb-8 text-muted-foreground max-w-2xl mx-auto animate-fade-in" style={{ animationDelay: '0.1s' }}>
            Rejoignez des centaines de membres satisfaits et atteignez vos objectifs fitness
          </p>
          <Link to="/register">
            <Button size="lg" className="bg-accent hover:bg-accent-glow text-accent-foreground font-bold px-8 py-6 text-lg transition-all hover:scale-105 hover:shadow-glow animate-scale-in" style={{ animationDelay: '0.2s' }}>
              Inscrivez-vous Maintenant
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-card border-t border-border py-8">
        <div className="container mx-auto px-4">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="flex items-center gap-3">
              <img src={logo} alt="JNB Fitness" className="h-8 w-auto object-contain" />
              <span className="text-xl font-bold text-accent">JNB FITNESS</span>
            </div>
            <p className="text-muted-foreground">© 2025 JNB Fitness. Tous droits réservés.</p>
            <div className="flex gap-4">
              <a href="#" className="text-muted-foreground hover:text-accent transition-colors">Facebook</a>
              <a href="#" className="text-muted-foreground hover:text-accent transition-colors">Instagram</a>
              <a href="#" className="text-muted-foreground hover:text-accent transition-colors">Twitter</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Landing;
