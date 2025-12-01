// Converted from TypeScript to JavaScript to avoid needing the TypeScript dependency

const URL = 'http://localhost:8080'

describe('Nordic Electronics SPA E2E Testing', () => {
  it('shows the landingpage and that it is loaded', () => {
    cy.visit(URL)
    cy.contains('Welcome to Nordic Electronics').should('be.visible')
    cy.get('button.cta-button').contains('Shop Now').should('be.visible')
  });

  // more tests
  it("", () => {

  });
});

